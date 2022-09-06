## 1. 概述

本教程介绍Reactor中的map和flatMap运算符。它们在Mono和Flux类中定义，用于在处理流时转换元素。

在接下来的部分中，**我们将重点关注Flux类中的map和flatMap方法**。Mono类的工作方式相同。

## 2. Maven依赖

```xml

<dependencies>
    <dependency>
        <groupId>io.projectreactor</groupId>
        <artifactId>reactor-core</artifactId>
        <version>3.4.17</version>
    </dependency>
</dependencies>
```

## 3. map操作

现在，让我们看看如何使用map运算符。

Flux#map方法需要一个Function参数，其简单如下：

```
Function<String, String> mapper = String::toUpperCase;
```

mapper函数将输入字符串转换为其大写形式。我们可以将其应用于Flux流：

```java
public class MappingUnitTest {

    @Test
    void givenInputStream_whenCallingTheMapOperator_thenItemsAreTransformed() {
        Function<String, String> mapper = String::toUpperCase;
        Flux<String> inFlux = Flux.just("tuyucheng", ".", "com");
        Flux<String> outFlux = inFlux.map(mapper);

        StepVerifier.create(outFlux)
                .expectNext("TUYUCHENG", ".", "COM")
                .expectComplete()
                .verify();
    }
}
```

给定的mapper函数**将输入流中的每个元素转换为输出中的新元素，同时保持顺序**。

请注意，**调用map方法时不会执行mapper函数。而是在我们订阅流时运行**。

## 4. flatMap操作

### 4.1 代码示例

与map类似，flatMap运算符带有一个Function类型的参数。但是，与使用map的函数不同，
**flatMap接收的mapper函数将输入项转换为Publisher而不是普通对象**。

以下是一个例子：

```
Function<String, Publisher<String>> mapper = s -> Flux.just(s.toUpperCase().split(""));
```

在这种情况下，mapper函数将字符串转换为其大写形式，然后将其拆分为单独的字符。最后，该函数从这些字符构建一个新流。

我们现在可以将给定的mapper传递给flatMap方法：

```java
public class MappingUnitTest {
    @Test
    void givenInputStream_whenCallingTheFlatMapOperator_thenItemsAreFlatten() {
        Function<String, Publisher<String>> mapper = s -> Flux.just(s.toUpperCase().split(""));
        Flux<String> inFlux = Flux.just("tuyucheng", ".", "com");
        Flux<String> outFlux = inFlux.flatMap(mapper);

        List<String> output = new ArrayList<>();
        outFlux.subscribe(output::add);
        assertThat(output).containsExactlyInAnyOrder("T", "U", "Y", "U", "C", "H", "E", "N", "G", ".", "C", "O", "M");
    }
}
```

flatMapping操作使用三个字符串元素从上游创建了三个新流。
之后，这三个流中的元素被拆分并交织在一起，形成另一个新的流。这个最终流包含来自所有三个输入字符串的字符。

请注意，由于来自不同源的元素交错，**它们在输出中的顺序可能与我们在输入中看到的不同**。

## 4.2 管道操作说明

我们刚刚定义了一个mapper，将它传递给一个flatMap操作，并在流上调用该操作符，现在我们深入探讨为什么输出中的元素可能会发生乱序。

首先，让我们明确一点，**在订阅流之前不会发生任何操作**。当订阅发生时，管道将执行并调用传递给flatMap方法的mapper函数。

此时，mapper对输入流中的元素执行必要的转换。**这些元素中的每一个都可以转换为多个元素，然后用于创建新的流**。
在我们的代码示例中，表达式Flux.just(s.toUpperCase().split(""))的返回值表示这样一个流。

一旦一个由Publisher实例表示的新流准备就绪，flatMap就会急切地订阅。
**运算符不会等到发布者完成后再转到下一个流**，这意味着订阅是非阻塞的。

由于管道同时处理所有派生流，因此它们的元素可能随时进入。结果就会导致原始顺序丢失。
如果元素的顺序很重要，请考虑改用flatMapSequential运算符。

## 5. map和flatMap之间的不同

到目前为止，我们已经介绍了map和flatMap运算符。让我们总结一下它们之间的主要区别。

### 5.1 一对一与一对多

**map运算符对流元素执行一对一的转换，而flatMap执行一对多的转换**。当我们观察方法签名时，这种区别很明显：

+ <V\> Flux<V\> map(Function<? super T, ? extends V> mapper) – mapper将单个T类型值转换为单个V类型值。

+ Flux<R\> flatMap(Function<? super T, ? extends Publisher<? extends R>> mapper) –
  mapper将T类型的单个值转换为R类型元素的发布者。

我们可以看到，在功能上，Reactor中的map和flatMap的区别类似于Java Stream API中的map和flatMap的区别。

### 5.2 同步与异步

以下是Reactor库的API规范中的两个摘要：

+ map：通过对每个元素应用同步函数来转换此Flux发出的元素。
+ flatMap：将此Flux发出的元素异步转换为Publishers。

不难看出，**map是一个同步运算符** - 它只是一种将一个值转换为另一个值的方法。此方法与调用方在同一线程中执行。

另一方面，**flatMap是异步的**。事实上，单个元素到Publisher的转换可以是同步的，也可以是异步的。

在我们的示例代码中，该操作是同步的，因为我们使用Flux#just方法发出元素。
但是，**在处理引入高延迟的源(例如远程服务器)时，异步处理是更好的选择**。

重要的一点是管道并不关心元素来自哪个线程 - 它只关注发布者本身。

## 6. 总结

在本文中，我们介绍了Reactor中的map和flatMap运算符。我们演示了几个例子并简要概括了两种运算符之间的区别。