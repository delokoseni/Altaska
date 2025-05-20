export function deleteProject(projectId, handleFetchWithToast, onSuccess) {
    if (!confirm("Вы уверены, что хотите удалить проект? Это действие необратимо.")) return;

    handleFetchWithToast(
        `/api/projects/${projectId}`,
        {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': csrfToken
            }
        },
        "Проект успешно удалён",
        "Ошибка при удалении проекта"
    )
    .then(() => onSuccess())
    .catch(error => console.error("Ошибка при удалении проекта:", error));
}

