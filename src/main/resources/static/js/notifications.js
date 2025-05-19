import { getCsrfToken } from './getCsrfToken.js';

const notificationTypeDict = {
  'add_task_performer': 'Вас назначили исполнителем',

};

document.addEventListener("DOMContentLoaded", () => {
  const bellButton = document.querySelector('.profile-button[href=""]');
  if (!bellButton) return;

  const notificationsPanel = document.createElement('div');
  notificationsPanel.id = 'notifications-panel';
  notificationsPanel.style.display = 'none';

  // Добавляем шапку
  const header = document.createElement('div');
  header.className = 'panel-header';
  header.textContent = 'Уведомления';
  notificationsPanel.appendChild(header);

  // Контейнер для списка уведомлений
  const contentContainer = document.createElement('div');
  contentContainer.className = 'notifications-content';
  notificationsPanel.appendChild(contentContainer);

  document.body.appendChild(notificationsPanel);

  function positionPanel() {
    const rect = bellButton.getBoundingClientRect();
    notificationsPanel.style.position = 'absolute';
    notificationsPanel.style.top = (window.scrollY + rect.bottom + 5) + 'px';

    // Задаем right, а не left
    const rightPos = window.innerWidth - (rect.right + window.scrollX);
    notificationsPanel.style.right = rightPos + 'px';

    // Убираем left, чтобы не конфликтовало
    notificationsPanel.style.left = 'auto';
  }


  async function loadNotifications() {
    try {
      const response = await fetch('/api/notifications');
      if (!response.ok) throw new Error('Ошибка загрузки уведомлений');
      const notifications = await response.json();

      contentContainer.innerHTML = '';

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
              <pre>${JSON.stringify(notification, null, 2)}</pre>
            </div>
          `;

          notifElem.addEventListener('click', async () => {
            const content = notifElem.querySelector('.notification-content');
            const isVisible = content.style.display === 'block';
            content.style.display = isVisible ? 'none' : 'block';

            if (!isVisible && !notification.isRead) {
              try {
                const csrfToken = getCsrfToken();
                const markResponse = await fetch(`/api/notifications/read/${notification.id}`,
                {
                    method: 'POST',
                    headers:
                    {
                        'X-CSRF-TOKEN': csrfToken
                    }
                });

                if (markResponse.ok) {
                  notifElem.classList.remove('unread');
                  const dot = notifElem.querySelector('.unread-dot');
                  if (dot) dot.remove();
                  notification.isRead = true;
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
});
