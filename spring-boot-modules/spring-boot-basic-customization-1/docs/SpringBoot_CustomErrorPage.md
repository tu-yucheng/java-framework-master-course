## 1. 概述

在本文中，我们将研究如何禁用和自定义Spring Boot应用程序的默认错误页面。

## 2. 禁用白标错误页面

首先，让我们看看如何通过将server.error.whitelabel.enabled属性设置为false来完全禁用白标错误页面：

```properties
server.error.whitelabel.enabled=false
```

将此属性添加到application.properties文件将禁用错误页面并显示源自底层应用程序容器(例如Tomcat)的简洁页面。

我们可以通过排除ErrorMvcAutoConfiguration bean来获得相同的结果：

```properties
#for Spring Boot 1.0
#spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration

#for Spring Boot 2.0
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
```

或者通过将以下此注解添加到主类：

```
@EnableAutoConfiguration(exclude = {ErrorMvcAutoConfiguration.class})
```

上面提到的所有方法都会禁用白标错误页面。这给我们留下了一个问题，谁来实际处理错误？

如上所述，它通常是底层应用程序容器。好消息是我们可以显示自定义错误页面。

## 3. 显示自定义错误页面

我们首先需要创建一个自定义的HTML错误页面。

由于我们使用的是Thymeleaf模板引擎，因此我们将文件保存为error.html：

```html
<!DOCTYPE html>
<html lang="en">
<body>
<h1> Something went wrong! </h1>
<h2>Our Engineers are on it.</h2>
<p><a href="/">Go Home</a></p>
</body>
</html>
```

如果我们将此文件保存在resources/templates目录中，它将被默认的Spring Boot的BasicErrorController自动识别。

通过一些样式，我们可以为用户提供一个更好看的错误页面：

<img src="../assets/img.png">

我们还可以使用我们希望它使用的HTTP状态码来命名文件，
例如在resources/templates/error中将文件保存为404.html意味着它将显式用于404错误。

### 3.1 自定义ErrorController

到目前为止的限制是我们无法在发生错误时运行自定义逻辑。为了实现这一点，我们必须创建一个ErrorController bean来替换默认的。

为此，我们必须创建一个实现ErrorController接口的类。此外，我们需要设置server.error.path属性为自定义路径以在发生错误时调用。

```java

@Controller
public class MyErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError() {
        //do something like logging
        return "error";
    }
}
```

在上面的代码片段中，我们还使用@Controller标注类并为指定为属性server.error.path的路径创建映射：

```properties
server.error.path=/error
```

这样，控制器可以处理对/error路径的调用。

在handleError()中，我们返回之前创建的自定义错误页面。如果我们现在触发404错误，将显示我们的自定义页面。

让我们进一步改进handleError()以显示不同错误类型的特定错误页面。

例如，我们可以为404和500分别创建错误页面。然后我们可以使用错误的HTTP状态码来确定一个合适的错误页面来显示：

```java

@Controller
public class MyErrorController implements ErrorController {

    @GetMapping(value = "/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error-404";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error-500";
            }
        }
        return "error";
    }
}
```

例如，对于404错误，我们将看到error-404.html页面：

<img src="../assets/img_1.png">

## 4. 总结

有了这些信息，我们现在可以更优雅地处理错误并向我们的用户展示一个美观的页面。