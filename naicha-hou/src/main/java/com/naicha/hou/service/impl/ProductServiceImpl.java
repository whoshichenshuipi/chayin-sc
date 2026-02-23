package com.naicha.hou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naicha.hou.common.ResultCode;
import com.naicha.hou.dto.ProductCreateDTO;
import com.naicha.hou.dto.ProductDTO;
import com.naicha.hou.dto.ProductQueryDTO;
import com.naicha.hou.entity.Product;
import com.naicha.hou.entity.ProductCategory;
import com.naicha.hou.entity.ProductSpec;
import com.naicha.hou.enums.ProductStatus;
import com.naicha.hou.exception.BusinessException;
import com.naicha.hou.mapper.ProductMapper;
import com.naicha.hou.mapper.ProductCategoryMapper;
import com.naicha.hou.mapper.ProductSpecMapper;
import java.math.BigDecimal;
import com.naicha.hou.service.ProductService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductSpecMapper productSpecMapper;
    private final ObjectMapper objectMapper;

    /**
     * 系统是否需要审核商品
     */
    private static final boolean NEED_AUDIT = false;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProduct(Long merchantId, ProductCreateDTO createDTO) {
        log.info("创建商品，商家ID: {}, 商品名称: {}", merchantId, createDTO.getName());

        // 检查分类是否存在
        ProductCategory category = productCategoryMapper.selectById(createDTO.getCategoryId());
        if (category == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "分类不存在");
        }

        // 构建商品实体
        Product product = new Product();
        BeanUtils.copyProperties(createDTO, product);
        product.setMerchantId(merchantId);
        
        // 设置价格字段：price = originalPrice（当前售价）
        product.setPrice(createDTO.getOriginalPrice());
        
        product.setSales(0);
        product.setImages(convertImagesToString(createDTO.getImages()));
        product.setPromotionTypes(convertPromotionTypesToString(createDTO.getPromotionTypes()));
        product.setIsPromotion(createDTO.getIsPromotion() != null && createDTO.getIsPromotion() ? 1 : 0);
        product.setNeedAudit(NEED_AUDIT ? 1 : 0);
        // 使用枚举值设置状态
        product.setStatus(NEED_AUDIT ? ProductStatus.AUDITING.getCode() : ProductStatus.ONLINE.getCode());
        product.setAuditStatus(NEED_AUDIT ? 0 : 1); // 0-未审核，1-审核通过
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        // 保存商品
        productMapper.insert(product);

        // 保存商品规格
        saveProductSpecs(product.getId(), createDTO);

        log.info("商品创建成功，商品ID: {}", product.getId());
        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProduct(Long productId, ProductCreateDTO createDTO) {
        log.info("更新商品，商品ID: {}", productId);

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "商品不存在");
        }

        // 更新商品信息
        BeanUtils.copyProperties(createDTO, product, "id", "merchantId", "sales", "createdAt");
        
        // 更新价格字段
        product.setPrice(createDTO.getOriginalPrice());
        
        product.setImages(convertImagesToString(createDTO.getImages()));
        product.setPromotionTypes(convertPromotionTypesToString(createDTO.getPromotionTypes()));
        product.setIsPromotion(createDTO.getIsPromotion() != null && createDTO.getIsPromotion() ? 1 : 0);
        product.setUpdatedAt(LocalDateTime.now());

        productMapper.updateById(product);

        // 更新商品规格（先删除旧的，再插入新的）
        productSpecMapper.delete(new LambdaQueryWrapper<ProductSpec>()
                .eq(ProductSpec::getProductId, productId));
        saveProductSpecs(productId, createDTO);

        log.info("商品更新成功，商品ID: {}", productId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProduct(Long productId) {
        log.info("删除商品，商品ID: {}", productId);

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "商品不存在");
        }

        // 逻辑删除
        productMapper.deleteById(productId);

        log.info("商品删除成功，商品ID: {}", productId);
        return true;
    }

    @Override
    public ProductDTO getProductById(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "商品不存在");
        }

        return convertToDTO(product);
    }

    @Override
    public IPage<ProductDTO> getProductList(ProductQueryDTO queryDTO) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getMerchantId() != null) {
            wrapper.eq(Product::getMerchantId, queryDTO.getMerchantId());
        }
        if (queryDTO.getCategoryId() != null) {
            wrapper.eq(Product::getCategoryId, queryDTO.getCategoryId());
        }
        if (queryDTO.getName() != null) {
            wrapper.like(Product::getName, queryDTO.getName());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Product::getStatus, queryDTO.getStatus());
        }

        // 库存状态筛选
        if ("low".equals(queryDTO.getStockStatus())) {
            wrapper.apply("stock <= stock_alert");
        } else if ("out".equals(queryDTO.getStockStatus())) {
            wrapper.eq(Product::getStock, 0);
        }

        wrapper.orderByDesc(Product::getCreatedAt);

        Page<Product> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<Product> productPage = productMapper.selectPage(page, wrapper);

        // 转换为DTO
        return productPage.convert(this::convertToDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean onSale(Long productId) {
        return updateStatus(productId, ProductStatus.ONLINE.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean offSale(Long productId) {
        return updateStatus(productId, ProductStatus.OFFLINE.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long productId, Integer status) {
        log.info("更新商品状态，商品ID: {}, 新状态: {}", productId, status);

        // 验证状态码是否有效
        if (!ProductStatus.isValid(status)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "无效的商品状态码: " + status);
        }

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "商品不存在");
        }

        // 验证状态转换是否合法
        Integer oldStatus = product.getStatus();
        if (!isValidStatusTransition(oldStatus, status)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, 
                String.format("不允许的状态转换：从 %s 转换到 %s", 
                    ProductStatus.getDescriptionByCode(oldStatus),
                    ProductStatus.getDescriptionByCode(status)));
        }

        product.setStatus(status);
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);

        log.info("商品状态更新成功，商品ID: {}, 旧状态: {}, 新状态: {}", 
            productId, 
            ProductStatus.getDescriptionByCode(oldStatus),
            ProductStatus.getDescriptionByCode(status));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adjustStock(Long productId, Integer stock) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "商品不存在");
        }

        product.setStock(stock);
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);

        log.info("商品库存调整成功，商品ID: {}, 库存: {}", productId, stock);
        return true;
    }

    /**
     * 保存商品规格
     */
    private void saveProductSpecs(Long productId, ProductCreateDTO createDTO) {
        List<ProductSpec> specs = new ArrayList<>();

        // 甜度选项
        if (createDTO.getSweetnessOptions() != null && !createDTO.getSweetnessOptions().isEmpty()) {
            for (String option : createDTO.getSweetnessOptions()) {
                ProductSpec spec = new ProductSpec();
                spec.setProductId(productId);
                spec.setSpecType("sweetness");
                spec.setSpecName(option);
                spec.setStatus(1);
                spec.setCreatedAt(LocalDateTime.now());
                spec.setUpdatedAt(LocalDateTime.now());
                specs.add(spec);
            }
        }

        // 温度选项
        if (createDTO.getTemperatureOptions() != null && !createDTO.getTemperatureOptions().isEmpty()) {
            for (String option : createDTO.getTemperatureOptions()) {
                ProductSpec spec = new ProductSpec();
                spec.setProductId(productId);
                spec.setSpecType("temperature");
                spec.setSpecName(option);
                spec.setStatus(1);
                spec.setCreatedAt(LocalDateTime.now());
                spec.setUpdatedAt(LocalDateTime.now());
                specs.add(spec);
            }
        }

        // 加料选项
        if (createDTO.getAddons() != null && !createDTO.getAddons().isEmpty()) {
            for (int i = 0; i < createDTO.getAddons().size(); i++) {
                ProductCreateDTO.AddonDTO addon = createDTO.getAddons().get(i);
                ProductSpec spec = new ProductSpec();
                spec.setProductId(productId);
                spec.setSpecType("addon");
                spec.setSpecName(addon.getName());
                spec.setExtraPrice(addon.getPrice());
                spec.setStock(addon.getStock());
                spec.setSort(i);
                spec.setStatus(1);

                // 将复杂数据转换为JSON
                Map<String, Object> specValue = new HashMap<>();
                specValue.put("name", addon.getName());
                specValue.put("price", addon.getPrice());
                specValue.put("stock", addon.getStock());

                spec.setSpecValue(convertObjectToString(specValue));
                spec.setCreatedAt(LocalDateTime.now());
                spec.setUpdatedAt(LocalDateTime.now());
                specs.add(spec);
            }
        }

        // 杯型规格
        if (createDTO.getSizes() != null && !createDTO.getSizes().isEmpty()) {
            for (int i = 0; i < createDTO.getSizes().size(); i++) {
                ProductCreateDTO.SizeDTO size = createDTO.getSizes().get(i);
                ProductSpec spec = new ProductSpec();
                spec.setProductId(productId);
                spec.setSpecType("size");
                spec.setSpecName(size.getName());
                spec.setExtraPrice(size.getPrice());
                spec.setSort(i);
                spec.setStatus(1);

                // 将复杂数据转换为JSON
                Map<String, Object> specValue = new HashMap<>();
                specValue.put("name", size.getName());
                specValue.put("price", size.getPrice());
                specValue.put("capacity", size.getCapacity());

                spec.setSpecValue(convertObjectToString(specValue));
                spec.setCreatedAt(LocalDateTime.now());
                spec.setUpdatedAt(LocalDateTime.now());
                specs.add(spec);
            }
        }

        if (!specs.isEmpty()) {
            for (ProductSpec spec : specs) {
                productSpecMapper.insert(spec);
            }
        }
    }

    /**
     * 转换为DTO
     */
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        BeanUtils.copyProperties(product, dto);
        
        // 设置当前售价
        dto.setPrice(product.getPrice());

        // 解析图片列表
        if (product.getImages() != null) {
            try {
                List<String> images = objectMapper.readValue(product.getImages(),
                        new TypeReference<List<String>>() {});
                dto.setImages(images);
            } catch (Exception e) {
                log.error("解析商品图片失败", e);
                dto.setImages(new ArrayList<>());
            }
        }

        // 解析促销类型
        if (product.getPromotionTypes() != null) {
            try {
                List<String> promotionTypes = objectMapper.readValue(product.getPromotionTypes(),
                        new TypeReference<List<String>>() {});
                dto.setPromotionTypes(promotionTypes);
            } catch (Exception e) {
                log.error("解析促销类型失败", e);
                dto.setPromotionTypes(new ArrayList<>());
            }
        }

        // 获取分类名称
        ProductCategory category = productCategoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            dto.setCategoryName(category.getName());
        }

        // 状态文本 - 使用枚举获取
        dto.setStatusText(getStatusText(product.getStatus()));

        // 查询商品规格
        List<ProductSpec> specs = productSpecMapper.selectByProductId(product.getId());
        List<String> sweetnessOptions = specs.stream()
                .filter(s -> "sweetness".equals(s.getSpecType()))
                .map(ProductSpec::getSpecName)
                .collect(Collectors.toList());

        List<String> temperatureOptions = specs.stream()
                .filter(s -> "temperature".equals(s.getSpecType()))
                .map(ProductSpec::getSpecName)
                .collect(Collectors.toList());

        List<ProductDTO.AddonDTO> addons = specs.stream()
                .filter(s -> "addon".equals(s.getSpecType()))
                .map(this::convertToAddonDTO)
                .collect(Collectors.toList());

        List<ProductDTO.SizeDTO> sizes = specs.stream()
                .filter(s -> "size".equals(s.getSpecType()))
                .map(this::convertToSizeDTO)
                .collect(Collectors.toList());

        dto.setSweetnessOptions(sweetnessOptions);
        dto.setTemperatureOptions(temperatureOptions);
        dto.setAddons(addons);
        dto.setSizes(sizes);

        return dto;
    }

    /**
     * 转换为加料DTO
     */
    private ProductDTO.AddonDTO convertToAddonDTO(ProductSpec spec) {
        ProductDTO.AddonDTO dto = new ProductDTO.AddonDTO();
        dto.setName(spec.getSpecName());
        dto.setPrice(spec.getExtraPrice());
        dto.setStock(spec.getStock());

        // 解析spec_value
        if (spec.getSpecValue() != null) {
            try {
                Map<String, Object> specValue = objectMapper.readValue(spec.getSpecValue(),
                        new TypeReference<Map<String, Object>>() {});
                dto.setName((String) specValue.get("name"));
                if (specValue.get("price") instanceof Number) {
                    dto.setPrice(BigDecimal.valueOf(((Number) specValue.get("price")).doubleValue()));
                }
                if (specValue.get("stock") instanceof Number) {
                    dto.setStock(((Number) specValue.get("stock")).intValue());
                }
            } catch (Exception e) {
                log.error("解析加料规格失败", e);
            }
        }

        return dto;
    }

    /**
     * 转换为杯型DTO
     */
    private ProductDTO.SizeDTO convertToSizeDTO(ProductSpec spec) {
        ProductDTO.SizeDTO dto = new ProductDTO.SizeDTO();
        dto.setName(spec.getSpecName());
        dto.setPrice(spec.getExtraPrice());

        // 解析spec_value
        if (spec.getSpecValue() != null) {
            try {
                Map<String, Object> specValue = objectMapper.readValue(spec.getSpecValue(),
                        new TypeReference<Map<String, Object>>() {});
                dto.setName((String) specValue.get("name"));
                dto.setCapacity((String) specValue.get("capacity"));
                if (specValue.get("price") instanceof Number) {
                    dto.setPrice(BigDecimal.valueOf(((Number) specValue.get("price")).doubleValue()));
                }
            } catch (Exception e) {
                log.error("解析杯型规格失败", e);
            }
        }

        return dto;
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        return ProductStatus.getDescriptionByCode(status);
    }

    /**
     * 验证状态转换是否合法
     * 
     * 状态转换规则：
     * - 任何状态都可以转换为"下架"(0)
     * - "审核中"(3) 可以转换为"上架"(1)、"审核不通过"(4)
     * - "上架"(1) 可以转换为"下架"(0)、"预售"(2)
     * - "下架"(0) 可以转换为"上架"(1)
     * - "审核不通过"(4) 可以转换为"审核中"(3)
     * - "预售"(2) 可以转换为"上架"(1)
     */
    private boolean isValidStatusTransition(Integer oldStatus, Integer newStatus) {
        // 下架(0)是万能目标状态，任何状态都可以转换到下架
        if (newStatus.equals(ProductStatus.OFFLINE.getCode())) {
            return true;
        }

        // 如果是相同的状态，不允许转换
        if (oldStatus.equals(newStatus)) {
            return false;
        }

        // 根据旧状态判断可以转换到的新状态
        switch (oldStatus) {
            case 0: // 下架 -> 只能转为上架
                return newStatus.equals(ProductStatus.ONLINE.getCode());
            
            case 1: // 上架 -> 可以转为下架、预售
                return newStatus.equals(ProductStatus.OFFLINE.getCode()) 
                    || newStatus.equals(ProductStatus.PRESALE.getCode());
            
            case 2: // 预售 -> 可以转为下架、上架
                return newStatus.equals(ProductStatus.OFFLINE.getCode()) 
                    || newStatus.equals(ProductStatus.ONLINE.getCode());
            
            case 3: // 审核中 -> 可以转为上架、审核不通过
                return newStatus.equals(ProductStatus.ONLINE.getCode()) 
                    || newStatus.equals(ProductStatus.AUDIT_FAILED.getCode())
                    || newStatus.equals(ProductStatus.OFFLINE.getCode());
            
            case 4: // 审核不通过 -> 可以转为审核中
                return newStatus.equals(ProductStatus.AUDITING.getCode())
                    || newStatus.equals(ProductStatus.OFFLINE.getCode());
            
            default:
                return false;
        }
    }

    /**
     * 转换图片列表为字符串
     */
    private String convertImagesToString(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (Exception e) {
            log.error("转换图片列表失败", e);
            return null;
        }
    }

    /**
     * 转换促销类型为字符串
     */
    private String convertPromotionTypesToString(List<String> promotionTypes) {
        if (promotionTypes == null || promotionTypes.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(promotionTypes);
        } catch (Exception e) {
            log.error("转换促销类型失败", e);
            return null;
        }
    }

    /**
     * 转换对象为字符串
     */
    private String convertObjectToString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("转换对象失败", e);
            return null;
        }
    }
}

