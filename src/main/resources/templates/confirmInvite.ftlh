<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Приглашение в проект</title>
    <link rel="stylesheet" href="/css/mainstyle.css">
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>
<header>
    <div class="site-title">altaska</div>
    <div><a href="/login" class="login-button">Войти</a></div>
</header>
<main style="display: flex; justify-content: center; align-items: center; height: 100vh; text-align: center;">
    <div class="content-block" style="text-align: center; margin-top: 80px;">
        <h1>${statusTitle}</h1>
        <p>${statusMessage}</p>
        <#if goToLogin??>
        <a href="/login" class="register-button" style="margin-top: 20px; display: inline-block;">Войти</a>
    </#if>
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
