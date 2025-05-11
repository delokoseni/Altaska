import { getCsrfToken } from './getCsrfToken.js';

const taskTimers = new Map();

export function loadExistingTimers() {
    for (let key in localStorage) {
        if (key.startsWith("timer_")) {
            const taskId = key.split("_")[1];
            const data = JSON.parse(localStorage.getItem(key));
            if (data.running) {
                resumeTimer(taskId, data);
            } else {
                updateTimerDisplay(taskId, data.timeSpent || 0);
            }
        }
    }
}

export function resumeTimer(taskId, data) {
    const startTimestamp = data.startTimestamp;
    const previous = data.timeSpent || 0;

    const interval = setInterval(() => {
        const elapsed = Math.floor((Date.now() - startTimestamp) / 1000);
        updateTimerDisplay(taskId, previous + elapsed);
    }, 1000);

    taskTimers.set(taskId, { startTimestamp, interval});
}

export function startTimer(taskId) {
    if (taskTimers.has(taskId)) return; // уже запущен

    const data = JSON.parse(localStorage.getItem(`timer_${taskId}`)) || {};
    const startTimestamp = Date.now();
    const timeSpent = data.timeSpent || 0;

    const interval = setInterval(() => {
        const elapsed = Math.floor((Date.now() - startTimestamp) / 1000);
        updateTimerDisplay(taskId, timeSpent + elapsed);
    }, 1000);

    taskTimers.set(taskId, { startTimestamp, interval});
    localStorage.setItem(`timer_${taskId}`, JSON.stringify({ running: true, startTimestamp, timeSpent }));
}

export function pauseTimer(taskId) {
    const timer = taskTimers.get(taskId);
    if (!timer) return;

    const elapsed = Math.floor((Date.now() - timer.startTimestamp) / 1000);
    const data = JSON.parse(localStorage.getItem(`timer_${taskId}`)) || {};
    const total = (data.timeSpent || 0) + elapsed;

    clearInterval(timer.interval);
    taskTimers.delete(taskId);

    localStorage.setItem(`timer_${taskId}`, JSON.stringify({ running: false, timeSpent: total }));
    updateTimerDisplay(taskId, total);
    sendToServer(taskId, total);
}

export function resetTimer(taskId) {
    pauseTimer(taskId);
    updateTimerDisplay(taskId, 0);
    localStorage.removeItem(`timer_${taskId}`);
    sendToServer(taskId, 0);
}

export function updateTimerDisplay(taskId, seconds) {
    const el = document.querySelector(`#timer-${taskId}`);
    if (el) el.textContent = formatTime(seconds);
}

export function formatTime(sec) {
    const h = String(Math.floor(sec / 3600)).padStart(2, '0');
    const m = String(Math.floor((sec % 3600) / 60)).padStart(2, '0');
    const s = String(sec % 60).padStart(2, '0');
    return `${h}:${m}:${s}`;
}

export function sendToServer(taskId, timeSpent) {
    fetch(`/api/tasks/${taskId}/time-spent`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': getCsrfToken()
        },
        body: JSON.stringify({ timeSpent })
    });
}

// Сохранение при закрытии/перезагрузке страницы
window.addEventListener("beforeunload", () => {
    taskTimers.forEach((timer, taskId) => {
        const elapsed = Math.floor((Date.now() - timer.startTimestamp) / 1000);
        const data = JSON.parse(localStorage.getItem(`timer_${taskId}`)) || {};
        const total = (data.timeSpent || 0) + elapsed;

        clearInterval(timer.interval);

        localStorage.setItem(`timer_${taskId}`, JSON.stringify({ running: false, timeSpent: total }));
        sendToServer(taskId, total);
    });
    taskTimers.clear();
});

window.addEventListener("DOMContentLoaded", loadExistingTimers);
