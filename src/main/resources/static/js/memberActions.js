export function removeProjectMember(projectId, userId, memberEmail, loadProjectInfoView) {
    if (!confirm(`Вы уверены, что хотите удалить участника ${memberEmail}?`)) {
        return;
    }

    fetch(`/api/projects/${projectId}/members/${userId}`, {
        method: 'DELETE',
        headers: {
            'X-CSRF-TOKEN': csrfToken
        }
    })
    .then(response => {
        if (response.ok) {
            alert('Участник удалён');
            loadProjectInfoView(projectId);
        } else {
            return response.text().then(text => {
                alert('Ошибка удаления: ' + text);
            });
        }
    })
    .catch(error => {
        console.error('Ошибка при удалении участника:', error);
        alert('Произошла ошибка');
    });
}
