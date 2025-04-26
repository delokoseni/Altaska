export function deleteTask(taskId, csrfToken) {
    const formData = new URLSearchParams();
    fetch(`/api/tasks/${taskId}/delete`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-CSRF-TOKEN': csrfToken
        },
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Ошибка при удалении задачи');
        }
        return response.json();
    })
    .then(data => {
        console.log('Задача удалена', data);
        window.location.reload();
    })
    .catch(err => {
        alert('Ошибка: ' + err.message);
    });
}
