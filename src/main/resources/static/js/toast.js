export function showToast(message, type = "success") {
    const toast = document.createElement("div");
    toast.classList.add("custom-toast", `toast-${type}`);

    const text = document.createElement("span");
    text.classList.add("toast-message");
    text.textContent = message;

    const closeBtn = document.createElement("span");
    closeBtn.classList.add("toast-close");
    closeBtn.innerHTML = "&times;";
    closeBtn.onclick = () => {
        toast.classList.remove("toast-show");
        toast.classList.add("toast-hide");
        toast.addEventListener("transitionend", () => toast.remove());
    };

    toast.appendChild(text);
    toast.appendChild(closeBtn);
    document.body.appendChild(toast);

    // Показ с анимацией
    setTimeout(() => toast.classList.add("toast-show"), 100);

    // Автоматическое скрытие
    setTimeout(() => {
        toast.classList.remove("toast-show");
        toast.classList.add("toast-hide");
        toast.addEventListener("transitionend", () => toast.remove());
    }, 5000);
}
