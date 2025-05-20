import { showToast } from './toast.js';

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
    .then(async response => {
        if (!response.ok) {
            const errorText = await response.text();
            const hasEnglish = /[a-zA-Z]/.test(errorText);
            if (hasEnglish) {
                showToast("Не удалось удалить задачу: неизвестная ошибка", "error");
            } else {
                showToast("Не удалось удалить задачу: " + errorText, "error");
            }
            throw new Error(errorText);
        }
        return response.json();
    })
    .then(data => {
        showToast("Задача успешно удалена");
        console.log("Задача удалена", data);
        setTimeout(() => window.location.reload(), 1000);
    })
    .catch(error => {
        const message = error.message || '';
        const hasEnglish = /[a-zA-Z]/.test(message);
        if (hasEnglish) {
            if (message === 'Failed to fetch') {
                showToast("Не удалось удалить задачу: проверьте подключение к интернету или попробуйте позже", "error");
            } else {
                showToast("Не удалось удалить задачу: неизвестная ошибка", "error");
            }
        } else {
            showToast("Не удалось удалить задачу: " + message, "error");
        }
        console.error("Ошибка удаления задачи:", error);
    });
}

