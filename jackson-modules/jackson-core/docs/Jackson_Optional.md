## 1. 介绍

在本文中，我们将概述Optional类，然后解释一些在与Jackson一起使用时可能遇到的问题。

在此之后，我们将介绍一种解决方案，让Jackson将Optional视为普通的可空对象。

## 2. 问题概述

首先，让我们看看当我们尝试使用Jackson序列化和反序列化Optional时会发生什么。

### 2.1 Maven依赖

要使用Jackson，我们可以添加以下依赖项：

```
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-core</artifactId>
    <version>2.13.0</version>
</dependency>
```

### 2.2 Book对象

然后，我们创建一个Book类，包含一个普通字段和一个Optional字段：

```java
public class Book {
    String title;
    Optional<String> subTitle;
    // getters and setters omitted
}
```

请记住，我们不应将Optionals用作类的字段，我们这样做是为了说明问题。

### 2.3 序列化

现在，我们实例化一个Book对象：

```
Book book = new Book();
book.setTitle("Oliver Twist");
book.setSubTitle(Optional.of("The Parish Boy's Progress"));
```

最后，让我们使用Jackson的ObjectMapper对其进行序列化：

```
ObjectMapper mapper = new ObjectMapper();
String result = mapper.writeValueAsString(book);
```

我们会看到subTitle字段的输出不包含它的值，而是一个嵌套的JSON对象，其中包含一个名为present的字段：

```json
{
    "title": "Oliver Twist",
    "subTitle": {
        "present": true
    }
}
```

虽然这可能看起来很奇怪，但它实际上是我们应该期待的。

在这种情况下，isPresent()是Optional类的公共getter方法。这意味着它将使用true或false的值进行序列化，具体取决于它是否为空。
这是Jackson的默认序列化行为。

但是在这里，我们想要的是实际要序列化的subTitle字段的值。

### 2.4 反序列化

现在，当我们尝试将对象反序列化为Optional。我们会得到了一个JsonMappingException异常：

```java
public class OptionalTypeUnitTest {

    @Test
    void givenJsonString_whenDeserializingIntoOptional_thenShouldThrowEx() {
        ObjectMapper mapper = new ObjectMapper();
        String bookJson = "{ \"title\": \"Oliver Twist\", \"subTitle\": \"foo\" }";
        assertThrows(JsonMappingException.class, () -> mapper.readValue(bookJson, Book.class));
    }
}
```

本质上，Jackson需要一个可以将subtitle的值作为参数的构造函数。

## 3. 解决方案

我们想要的是让Jackson将空Optional视为null，并将当前Optional视为表示其值的字段。

幸运的是，这个问题已经为我们解决了。Jackson有一组处理JDK 8数据类型的模块，包括Optional。

### 3.1 Maven依赖和注册

首先，让我们添加所需的Maven依赖项：

```
<dependency>
   <groupId>com.fasterxml.jackson.datatype</groupId>
   <artifactId>jackson-datatype-jdk8</artifactId>
   <version>2.13.0</version>
</dependency>
```

现在，我们需要做的就是使用ObjectMapper注册Module：

```
ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());
```

### 3.2 序列化

现在，如果我们再次尝试序列化Book对象，我们现在会看到有一个subTitle字段，而不是嵌套的JSON：

```java
public class OptionalTypeUnitTest {

    ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());

    @Test
    void givenPresentOptional_whenSerializing_thenValueInJson() throws JsonProcessingException {
        String subTitle = "The Parish Boy's Progress";
        Book book = new Book();
        book.setTitle("Oliver Twist");
        book.setSubTitle(Optional.of(subTitle));

        String result = mapper.writeValueAsString(book);

        assertThat(from(result).getString("subTitle")).isEqualTo(subTitle);
    }
}
```

如果我们尝试序列化一个空的subTitle，它将被存储为null：

```java
public class OptionalTypeUnitTest {
    @Test
    void givenEmptyOptional_whenSerializing_thenNullValue() throws JsonProcessingException {
        Book book = new Book();
        book.setTitle("Oliver Twist");
        book.setSubTitle(Optional.empty());

        String result = mapper.writeValueAsString(book);

        assertThat(from(result).getString("subTitle")).isNull();
    }
}
```

### 3.3 反序列化

现在，我们再次测试反序列化Json，此时不会得到JsonMappingException：

```java
public class OptionalTypeUnitTest {
    @Test
    void givenField_whenDeserializingIntoOptional_thenIsPresentWithValue() throws IOException {
        String subTitle = "The Parish Boy's Progress";
        String book = "{ \"title\": \"Oliver Twist\", \"subTitle\": \"" + subTitle + "\" }";

        Book result = mapper.readValue(book, Book.class);

        assertThat(result.getSubTitle()).isEqualTo(Optional.of(subTitle));
    }
}
```

最后，如果Json中subTitle字段的值为空，此时反序列化会将其解析为一个空的Optional：

```java
public class OptionalTypeUnitTest {
    @Test
    void givenNullField_whenDeserializingIntoOptional_thenIsEmpty() throws IOException {
        String book = "{ \"title\": \"Oliver Twist\", \"subTitle\": null }";
        Book result = mapper.readValue(book, Book.class);

        assertThat(result.getSubTitle()).isEmpty();
    }
}
```

## 4. 总结

我们演示了如何通过利用jackson-datatype-jdk8来解决序列化与反序列化Optional对象的问题，
介绍了它如何使Jackson能够将空Optional视为null，并将当前Optional视为普通字段。