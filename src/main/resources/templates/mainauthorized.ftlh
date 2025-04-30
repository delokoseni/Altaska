<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="/css/mainAuthorized.css">
    <link rel="stylesheet" href="/css/createTask.css">
    <link rel="stylesheet" href="/css/createProject.css">
    <link rel="stylesheet" href="/css/sidebar.css">
    <link rel="stylesheet" href="/css/taskdetailssidebar.css">
    <link rel="stylesheet" href="/css/comments.css">
    <script src="/js/themeToggle.js" defer></script>
    <script src="/js/sidebar.js" defer></script>
    <script src="/js/mainAuthorized.js" defer></script>
    <script type = "module" src="/js/projectSelection.js"></script>
    <script type = "module" src="/js/taskDetails.js"></script>
    <title>Главная</title>
</head>
<body>
<div class="layout">
    <aside class="sidebar">
        <a href="/profile" class="profile-button">
            <img src="/icons/profile.svg" alt="Профиль">
        </a>

        <div class="sidebar-header">
            <h2>Проекты</h2>
            <div class="add-button-container">
                <button class="add-project-button" id="show-create-form">
                    +
                    <span class="add-label">Добавить</span>
                </button>
            </div>
        </div>
        <#if projects?? && (projects?size > 0)>
            <#list projects as project>
                <div class="project-entry">
                    <button class="project-button" onclick="selectProject('${project.getId()}', '${project.getName()}')">
                        ${project.getName()}
                    </button>
                </div>
            </#list>
        <#else>
            <div class="no-projects-message">Нет проектов</div>
        </#if>
    </aside>
<section class="main-content"></section>
</div>
<script>
    const csrfParam = '${_csrf.parameterName}';
    const csrfToken = '${_csrf.token}';
</script>
</body>
</html>