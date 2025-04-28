export function initTaskCommentsSection(taskId, container, currentUserEmail) {
    const commentsWrapper = document.createElement('div');
    commentsWrapper.className = 'task-comments-section';

    const title = document.createElement('h3');
    title.textContent = 'Комментарии';
    title.style.textAlign = 'center';
    commentsWrapper.appendChild(title);

    const commentsList = document.createElement('div');
    commentsList.className = 'comments-list';
    commentsWrapper.appendChild(commentsList);

    container.appendChild(commentsWrapper);
    commentsList.innerHTML = '';

    fetch(`/api/comments/task/${taskId}`)
        .then(response => response.json())
        .then(comments => {
            commentsList.innerHTML = '';

            if (comments.length === 0) {
                const noComments = document.createElement('div');
                noComments.className = 'no-comments';
                noComments.textContent = 'Нет комментариев к задаче';
                commentsList.appendChild(noComments);
            } else {
                comments.forEach(comment => {
                    const commentBlock = document.createElement('div');
                    commentBlock.className = 'comment-block';

                    const author = document.createElement('div');
                    author.className = 'comment-author';
                    author.textContent = comment.authorName;

                    const text = document.createElement('div');
                    text.className = 'comment-text';
                    text.textContent = comment.text;

                    commentBlock.appendChild(author);
                    commentBlock.appendChild(text);

                    // Если это комментарий текущего пользователя, добавляем кнопки "Редактировать" и "Удалить"
                    if (comment.authorName === currentUserEmail) {
                        const editButton = document.createElement('button');
                        editButton.textContent = 'Редактировать';
                        editButton.className = 'edit-comment-button';
                        editButton.addEventListener('click', () => editComment(comment.id, comment.text, taskId, commentsList, currentUserEmail));

                        const deleteButton = document.createElement('button');
                        deleteButton.textContent = 'Удалить';
                        deleteButton.className = 'delete-comment-button';
                        deleteButton.addEventListener('click', () => deleteComment(comment.id, commentsList));

                        commentBlock.appendChild(editButton);
                        commentBlock.appendChild(deleteButton);
                    }

                    commentsList.appendChild(commentBlock);
                });
            }
        })
        .catch(error => {
            console.error('Ошибка загрузки комментариев:', error);
            commentsList.innerHTML = '<div class="no-comments">Ошибка загрузки комментариев</div>';
        });
}

export function initCommentInputSection(taskId, container, onCommentAdded, csrfToken) {
    const inputWrapper = document.createElement('div');
    inputWrapper.className = 'comment-input-section';

    const textarea = document.createElement('textarea');
    textarea.className = 'comment-textarea';
    textarea.placeholder = 'Введите ваш комментарий...';

    const sendButton = document.createElement('button');
    sendButton.className = 'comment-send-button';
    sendButton.textContent = 'Отправить';

    sendButton.addEventListener('click', () => {
        const text = textarea.value.trim();
        if (text.length === 0) {
            alert('Комментарий не может быть пустым!');
            return;
        }

        sendComment(taskId, text, csrfToken)
            .then(() => {
                textarea.value = ''; // очистить поле
                if (onCommentAdded) {
                    onCommentAdded(); // обновить список комментариев
                }
            })
            .catch(error => {
                console.error('Ошибка отправки комментария:', error);
                alert('Ошибка отправки комментария');
            });
    });

    inputWrapper.appendChild(textarea);
    inputWrapper.appendChild(sendButton);
    container.appendChild(inputWrapper);
}

// Функция отправки комментария на сервер
export function sendComment(taskId, text, csrfToken) {
    return fetch('/api/comments/add-comment', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': csrfToken
        },
        body: JSON.stringify({
            taskId: taskId,
            content: text
        })
    }).then(response => {
        if (!response.ok) {
            throw new Error('Ошибка сервера');
        }
    });
}

function editComment(commentId, currentText, taskId, commentsList, currentUserEmail) {
    const newText = prompt('Редактировать комментарий:', currentText);
    if (newText !== null) {
        fetch(`/api/comments/edit-comment/${commentId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken // Передайте CSRF токен
            },
            body: JSON.stringify(newText)
        })
        .then(response => {
            if (response.ok) {
                initTaskCommentsSection(taskId, commentsList, currentUserEmail);  // Обновляем комментарии
            } else {
                alert('Ошибка редактирования комментария');
            }
        })
        .catch(error => {
            console.error('Ошибка редактирования комментария:', error);
        });
    }
}

// Функция для удаления комментария
function deleteComment(commentId, commentsList) {
    if (confirm('Вы уверены, что хотите удалить этот комментарий?')) {
        fetch(`/api/comments/delete-comment/${commentId}`, {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': csrfToken // Передайте CSRF токен
            }
        })
        .then(response => {
            if (response.ok) {
                commentsList.innerHTML = '';  // Очищаем список и перезагружаем комментарии
                initTaskCommentsSection(taskId, commentsList, currentUserEmail);
            } else {
                alert('Ошибка удаления комментария');
            }
        })
        .catch(error => {
            console.error('Ошибка удаления комментария:', error);
        });
    }
}