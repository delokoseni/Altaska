export function initTaskCommentsSection(taskId, container) {
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
                    commentsList.appendChild(commentBlock);
                });
            }
        })
        .catch(error => {
            console.error('Ошибка загрузки комментариев:', error);
            commentsList.innerHTML = '<div class="no-comments">Ошибка загрузки комментариев</div>';
        });
}
