# User-service
[] - Настроить consul config and discovery сервер; WARN
[] - Дописать CustomAttribute и разобраться нужна ли обратная ссылка на Product
[] - Написать unit тесты на сервисы
[] - Подключить объектное хранилище для фото продуктов
[] - Настроить spring aop для логирования и т.д
[] - Реализовать сервис ProductSearchService

```java
private Product product;
private Inventory inventory;

@BeforeEach
void initData() {
    Brand brand = Brand.builder()
            .id(1L)
            .name("Apple")
            .slug("apple")
            .description("Apple is a multinational technology company known for its consumer electronics like the iPhone, iPad, and Mac computers, as well as its software and online services")
            .build();

    Category primaryCategory = Category.builder()
            .id(1L)
            .name("Electronics")
            .slug("electronics")
            .description("electronics")
            .build();

    Category category1 = Category.builder()
            .id(2L)
            .name("Smartphones and telephones")
            .slug("smartphones-and-telephones")
            .description("Smartphones and telephones")
            .parent(primaryCategory)
            .build();

    Category category2 = Category.builder()
            .id(3L)
            .name("Apple iPhone")
            .slug("apple-iphone")
            .description("Apple iPhone")
            .parent(category1)
            .build();

    Set<Category> categories = new HashSet<>() {
        {
            add(category1);
            add(category2);
        }
    };

    Attribute attribute = Attribute.builder()
            .id(1L)
            .name("Color")
            .slug("color")
            .build();

    AttributeValue attributeValue = AttributeValue.builder()
            .id(1L)
            .attribute(attribute)
            .value("Red")
            .slug("red")
            .build();

    ProductAttributeValue productAttributeValue = ProductAttributeValue.builder()
            .id(1L)
            .attributeValue(attributeValue)
            .build();

    CustomAttribute customAttribute = CustomAttribute.builder()
            .id(1L)
            .name("Camera")
            .value("3")
            .build();

    product = Product.builder()
            .id(1L)
            .sku("23123-IP-AP")
            .name("Iphone 14")
            .brand(brand)
            .primaryCategory(primaryCategory)
            .description("The iPhone 14 is a 6.1-inch smartphone featuring a Super Retina XDR display, the A15 Bionic chip, and a dual-camera system with 12MP Main and Ultra Wide cameras.")
            .longDescription("The iPhone 14 is a 6.1-inch smartphone featuring a Super Retina XDR display, the A15 Bionic chip, and a dual-camera system with 12MP Main and Ultra Wide cameras. It was introduced in 2022 and includes features like Crash Detection and Emergency SOS via satellite. The device has a durable design, IP68 water and dust resistance, and supports wireless charging via MagSafe and Q")
            .basePrice(BigDecimal.valueOf(59999.99))
            .widthCm(9.0)
            .lengthCm(18.0)
            .weightKg(0.3)
            .heightCm(4.0)
            .rating(4.7)
            .ratingCount(100L)
            .build();

    productAttributeValue.setProduct(product);
    
    product.addAttributeValue(attributeValue);
    product.addCustomAttributes(customAttribute);
    
    categories.forEach(cat -> product.addCategory(cat));
    
    inventory = Inventory.builder()
            .id(1L)
            .product(product)
            .quantity(100)
            .lowStockThreshold(10)
            .build();
}
```