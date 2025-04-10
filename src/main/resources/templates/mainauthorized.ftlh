<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="/css/mainstyle.css">
    <link rel="stylesheet" href="/css/style.css">
    <link rel="stylesheet" href="/css/sidebar.css">
    <script src="/js/themeToggle.js" defer></script>
    <title>Главная</title>
</head>
<body>
<header>
    <div class="site-title">altaska</div>
    <div class="menu-container">
        <div class="theme-icon-container">
            <img class="theme-toggle" src="/icons/sun.svg" alt="Сменить тему">
        </div>
    </div>
    <div>
        <a href="/" class="register-button">Мой профиль</a>
    </div>
</header>
<main>
    <aside class="sidebar">
        <div class="sidebar-header">
            <h2>Проекты</h2>
            <div class="add-button-container">
                <button class="add-project-button" title="Добавить">
                    +
                    <span class="add-label">Добавить</span>
                </button>
            </div>
        </div>
        <#if projects?? && (projects?size > 0)>
            <#list projects as project>
                <div class="project-entry">
                    ${project.GetName()}
                </div>
            </#list>
        <#else>
            <div class="no-projects-message">Нет проектов</div>
        </#if>
</aside>
    <section class="main-content">
        <h1>Добро пожаловать!</h1>
    </section>
</main>
<footer>

</footer>
</body>
</html>