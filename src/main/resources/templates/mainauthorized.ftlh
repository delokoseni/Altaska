<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="csrf-token" content="${_csrf.token}">
    <link href="https://cdn.syncfusion.com/ej2/29.1.33/material.css" rel="stylesheet" />
    <script src="https://cdn.syncfusion.com/ej2/29.1.33/dist/ej2.min.js"></script>
    <script>
        ej.base.registerLicense('${SYNCFUSION_LICENSE}');
    </script>
    <link rel="stylesheet" href="/css/mainAuthorized.css">
    <link rel="stylesheet" href="/css/createTask.css">
    <link rel="stylesheet" href="/css/createProject.css">
    <link rel="stylesheet" href="/css/sidebar.css">
    <link rel="stylesheet" href="/css/taskdetailssidebar.css">
    <link rel="stylesheet" href="/css/loadProjectInfoView.css">
    <link rel="stylesheet" href="/css/kanban.css">
    <link rel="stylesheet" href="/css/toast.css">
    <link rel="stylesheet" href="/css/activitylog.css">
    <link rel="stylesheet" href="/css/notifications.css">
    <script src="/js/themeToggle.js" defer></script>
    <script src="/js/sidebar.js" defer></script>
    <script src="/js/mainAuthorized.js" defer></script>
    <script type = "module" src="/js/projectSelection.js"></script>
    <script type = "module" src="/js/taskDetails.js"></script>
    <script type = "module" src="/js/notifications.js" defer></script>
    <title>Главная</title>
</head>
<body>
<div class="layout">
    <aside class="sidebar">
        <div class="top-right-buttons">
            <a href="" class="profile-button" title="Уведомления">
                <img src="/icons/bell.svg" alt="Уведомления">
                <span class="notification-dot" style="display:none;"></span>
            </a>
            <a href="/profile" class="profile-button" title="Профиль">
                <img src="/icons/profile.svg" alt="Профиль">
            </a>
        </div>

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