export function deleteProject(projectId, onSuccess) {
    if (!confirm("Вы уверены, что хотите удалить проект? Это действие необратимо.")) return;

    fetch(`/api/projects/${projectId}`, {
        method: 'DELETE',
        headers: {
            'X-CSRF-TOKEN': csrfToken
        }
    })
    .then(res => {
        if (res.ok) {
            alert("Проект удалён.");
            onSuccess(); // например: возвращение на главную страницу
        } else {
            return res.text().then(text => {
                alert("Ошибка удаления: " + text);
            });
        }
    })
    .catch(err => {
        console.error("Ошибка при удалении проекта:", err);
        alert("Произошла ошибка при удалении проекта.");
    });
}
