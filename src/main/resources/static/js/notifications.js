import { getCsrfToken } from './getCsrfToken.js';

const notificationTypeDict = {
  'add_task_performer': 'Вас назначили исполнителем ',
  'remove_task_performer': 'Вас сняли с исполнения ',
  'assign_task_performer': 'Исполнитель взял задачу ',
  'unassign_task_performer': 'Исполнитель отказался от задачи ',
  'edit_task_name': 'Изменено название задачи ',
  'edit_task_description': 'Изменено описание задачи ',
  'edit_task_priority': 'Изменен приоритет задачи ',
  'edit_task_status': 'Изменен статус задачи ',
  'edit_task_deadline': 'Изменен дедлайн задачи ',
  'edit_task_start_time': 'Изменена дата начала задачи ',
  'delete_task': 'Задача была удалена',
  'change_role': 'Ваша роль в проекте была изменена ',
  'task_comment': 'Добавлен новый комментарий',
  'task_attachment': 'Добавлен новый файл',
  'leave_user': 'Пользователь покинул проект'
};

async function checkUnreadNotifications(bellButton) {
  try {
    const response = await fetch('/api/notifications');
    if (!response.ok) throw new Error('Ошибка загрузки уведомлений');
    const notifications = await response.json();
    const notificationDot = bellButton.querySelector('.notification-dot');

    if (notifications.some(n => !n.isRead)) {
      notificationDot.style.display = 'block';
    } else {
      notificationDot.style.display = 'none';
    }
  } catch (e) {
    console.error('Ошибка при проверке непрочитанных уведомлений', e);
  }
}

document.addEventListener("DOMContentLoaded", () => {
  const bellButton = document.querySelector('.profile-button[href=""]');
  if (!bellButton) return;

  // Если точка для уведомлений не создана, создаём её
  if (!bellButton.querySelector('.notification-dot')) {
    const dot = document.createElement('span');
    dot.className = 'notification-dot';
    dot.style.display = 'none';
    bellButton.appendChild(dot);
  }

  checkUnreadNotifications(bellButton);

  const notificationsPanel = document.createElement('div');
  notificationsPanel.id = 'notifications-panel';
  notificationsPanel.style.display = 'none';

  const header = document.createElement('div');
  header.className = 'panel-header';

  const title = document.createElement('span');
  title.textContent = 'Уведомления';

  const clearButton = document.createElement('button');
  clearButton.textContent = 'Очистить';
  clearButton.className = 'clear-notifications-btn';

  clearButton.addEventListener('click', async () => {
    if (!confirm("Удалить все уведомления?")) return;

    try {
      const csrfToken = getCsrfToken();
      const response = await fetch('/api/notifications/clear', {
        method: 'DELETE',
        headers: {
          'X-CSRF-TOKEN': csrfToken
        }
      });

      if (response.ok) {
        contentContainer.innerHTML = '<div class="no-notifications">Уведомлений нет</div>';
        const dot = bellButton.querySelector('.notification-dot');
        if (dot) dot.style.display = 'none';
      } else {
        alert("Не удалось очистить уведомления");
      }
    } catch (e) {
      console.error('Ошибка при очистке уведомлений', e);
      alert("Ошибка при очистке уведомлений");
    }
  });

  header.appendChild(title);
  header.appendChild(clearButton);

  notificationsPanel.appendChild(header);

  const contentContainer = document.createElement('div');
  contentContainer.className = 'notifications-content';
  notificationsPanel.appendChild(contentContainer);

  document.body.appendChild(notificationsPanel);

  function positionPanel() {
    const rect = bellButton.getBoundingClientRect();
    notificationsPanel.style.position = 'absolute';
    notificationsPanel.style.top = (window.scrollY + rect.bottom + 5) + 'px';

    const rightPos = window.innerWidth - (rect.right + window.scrollX);
    notificationsPanel.style.right = rightPos + 'px';
    notificationsPanel.style.left = 'auto';
  }

  async function loadNotifications() {
    try {
      const response = await fetch('/api/notifications');
      if (!response.ok) throw new Error('Ошибка загрузки уведомлений');
      const notifications = await response.json();

      contentContainer.innerHTML = '';

      const notificationDot = bellButton.querySelector('.notification-dot');

      if (notifications.some(n => !n.isRead)) {
        notificationDot.style.display = 'block';
      } else {
        notificationDot.style.display = 'none';
      }

      if (notifications.length === 0) {
        contentContainer.innerHTML = '<div class="no-notifications">Уведомлений нет</div>';
      } else {
        notifications.forEach(notification => {
          const notifElem = document.createElement('div');
          notifElem.className = 'notification-item';
          if (!notification.isRead) notifElem.classList.add('unread');

          notifElem.innerHTML = `
            <div class="notification-header">
              <span class="notification-type">${notificationTypeDict[notification.type] || notification.type} </span>
              <span class="notification-date">${new Date(notification.createdAt).toLocaleString()}</span>
              ${!notification.isRead ? '<span class="unread-dot"></span>' : ''}
            </div>
            <div class="notification-content" style="display:none;">
              ${notification.content ? notification.content : ''}
            </div>
          `;

          notifElem.addEventListener('click', async () => {
            const content = notifElem.querySelector('.notification-content');
            const isVisible = content.style.display === 'block';
            content.style.display = isVisible ? 'none' : 'block';

            if (!isVisible && !notification.isRead) {
              try {
                const csrfToken = getCsrfToken();
                const markResponse = await fetch(`/api/notifications/read/${notification.id}`, {
                  method: 'POST',
                  headers: {
                    'X-CSRF-TOKEN': csrfToken
                  }
                });

                if (markResponse.ok) {
                  notifElem.classList.remove('unread');
                  const dot = notifElem.querySelector('.unread-dot');
                  if (dot) dot.remove();
                  notification.isRead = true;

                  // Обновляем точку уведомлений — скрываем, если непрочитанных нет
                  const anyUnread = [...contentContainer.querySelectorAll('.notification-item.unread')].length > 0;
                  const notificationDot = bellButton.querySelector('.notification-dot');
                  notificationDot.style.display = anyUnread ? 'block' : 'none';
                }
              } catch (e) {
                console.error('Ошибка при отметке уведомления прочитанным', e);
              }
            }
          });

          contentContainer.appendChild(notifElem);
        });
      }

      notificationsPanel.style.display = 'block';
      positionPanel();

    } catch (e) {
      contentContainer.innerHTML = '<div class="error">Ошибка загрузки уведомлений</div>';
      console.error(e);
      notificationsPanel.style.display = 'block';
      positionPanel();
    }
  }

  bellButton.addEventListener('click', e => {
    e.preventDefault();
    if (notificationsPanel.style.display === 'none' || notificationsPanel.style.display === '') {
      loadNotifications();
    } else {
      notificationsPanel.style.display = 'none';
    }
  });

  document.addEventListener('click', e => {
    if (!notificationsPanel.contains(e.target) && !bellButton.contains(e.target)) {
      notificationsPanel.style.display = 'none';
    }
  });

  window.addEventListener('resize', () => {
    if (notificationsPanel.style.display === 'block') positionPanel();
  });

  window.addEventListener('scroll', () => {
    if (notificationsPanel.style.display === 'block') positionPanel();
  });

  // Периодическая проверка новых уведомлений каждые 30 секунд
  async function pollNotifications() {
    try {
      const response = await fetch('/api/notifications');
      if (!response.ok) throw new Error('Ошибка загрузки уведомлений');
      const notifications = await response.json();

      const notificationDot = bellButton.querySelector('.notification-dot');
      if (notifications.some(n => !n.isRead)) {
        notificationDot.style.display = 'block';
      } else {
        notificationDot.style.display = 'none';
      }

      // Если панель открыта, обновим список уведомлений
      if (notificationsPanel.style.display === 'block') {
        // Можно вызвать loadNotifications() или обновить contentContainer напрямую
        await loadNotifications();
      }
    } catch (e) {
      console.error('Ошибка при периодической проверке уведомлений', e);
    }
  }

  setInterval(pollNotifications, 30000);
});