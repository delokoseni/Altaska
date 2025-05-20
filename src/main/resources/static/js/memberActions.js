export function removeProjectMember(projectId, userId, memberEmail, handleFetchWithToast, loadProjectInfoView) {
    if (!confirm(`Вы уверены, что хотите удалить участника ${memberEmail}?`)) {
        return;
    }

    handleFetchWithToast(
        `/api/projects/${projectId}/members/${userId}`,
        {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': csrfToken
            }
        },
        'Участник успешно удалён',
        'Ошибка при удалении участника'
    )
    .then(() => loadProjectInfoView(projectId))
    .catch(error => {
        console.error('Ошибка при удалении участника:', error);
    });
}
