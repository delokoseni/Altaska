<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="/css/mainstyle.css">
    <link rel="stylesheet" href="/css/style.css">
    <link rel="stylesheet" href="/css/registrationlogin.css">
    <script src="/js/themeToggle.js" defer></script>
    <script src="/js/passwordToggle.js"></script>
    <title>Регистрация</title>
</head>
<body>
<header>
    <div class="site-title">altaska</div>
    <div>
        <a href="/login" class="login-button">Войти</a>
        <a href="/" class="register-button">На главную</a>
    </div>
</header>
<main>
    <div class="container">
        <h1>Регистрация</h1>
    <#if error??>
        <div class="error-message">
            <p>${error}</p>
        </div>
    </#if>
    <form action="/registration" method="post">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <label>Email <span class="required" title="Поле обязательно для заполнения">*</span></label>
        <input type="text" name="email" placeholder="Email" required>
        <label>Пароль <span class="required" title="Поле обязательно для заполнения">*</span></label>
        <div class="password-container">
            <input type="password" id="password" name="password" placeholder="Пароль" required>
            <span class="toggle-password" onclick="togglePasswordVisibility('password')" title="Показать/Скрыть пароль">
                    <img id="eye-icon-password" src="/icons/EyeSlashed.svg" class="eye-icon" alt="Глаз">
                </span>
        </div>
        <label>Повторите пароль <span class="required" title="Поле обязательно для заполнения">*</span></label>
        <div class="password-container">
            <input type="password" id="repeatpassword" name="repeatpassword" placeholder="Повторите пароль" required>
            <span class="toggle-password" onclick="togglePasswordVisibility('repeatpassword')" title="Показать/Скрыть пароль">
                    <img id="eye-icon-repeatpassword" src="/icons/EyeSlashed.svg" class="eye-icon" alt="Глаз">
                </span>
        </div>
        <button type="submit">Зарегистрироваться</button>
    </form>
    </div>
</main>
<footer>
    <p>
        Разработчик: Бельш А.Е.<br>
        Алтайский государственный технический университет<br>
        Барнаул, 2025
    </p>
</footer>
</body>
</html>