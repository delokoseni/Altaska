export async function createPerformersSection(taskId, projectId, containerElement, csrfToken) {
    containerElement.innerHTML = "";

    // Заголовок
    const title = document.createElement("h3");
    title.textContent = "Исполнители";
    containerElement.appendChild(title);

    // Загружаем исполнителей и участников проекта
    const performers = await fetch(`/api/task-performers/${taskId}`).then(r => r.json());
    const members = await fetch(`/api/projects/${projectId}/confirmed-members`).then(r => r.json());

    // Отображаем исполнителей или "Нет исполнителей"
    if (performers.length === 0) {
        const empty = document.createElement("p");
        empty.textContent = "Нет исполнителей";
        containerElement.appendChild(empty);
    } else {
        const list = document.createElement("ul");
        performers.forEach(p => {
            const li = document.createElement("li");
            li.textContent = p.email + " ";

            const delBtn = document.createElement("button");
            delBtn.textContent = "Удалить";
            delBtn.onclick = async () => {
                await fetch(`/api/task-performers/${taskId}?userId=${p.userId}`, {
                    method: "DELETE",
                    headers: { 'X-CSRF-TOKEN': csrfToken }
                });
                createPerformersSection(taskId, projectId, containerElement, csrfToken);
            };

            li.appendChild(delBtn);
            list.appendChild(li);
        });
        containerElement.appendChild(list);
    }

    // Выпадающий список с участниками
    const select = document.createElement("select");
    members.forEach(m => {
        const opt = document.createElement("option");
        opt.value = m.userId; // т.к. ты возвращаешь { userId, email }
        opt.textContent = m.email;
        select.appendChild(opt);
    });
    containerElement.appendChild(select);

    // Кнопка "Назначить исполнителем"
    const addBtn = document.createElement("button");
    addBtn.textContent = "Назначить исполнителем";
    addBtn.onclick = async () => {
        const userId = select.value;

        try {
            const response = await fetch(`/api/task-performers/${taskId}`, {
                method: "POST",
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-CSRF-TOKEN': csrfToken
                },
                body: new URLSearchParams({ userId })
            });

            if (!response.ok) {
                const errorMessage = await response.text();
                alert("Ошибка: " + errorMessage);
                return;
            }

            createPerformersSection(taskId, projectId, containerElement, csrfToken);
        } catch (error) {
            console.error("Ошибка при назначении исполнителя:", error);
            alert("Произошла ошибка при назначении исполнителя.");
        }
    };
    containerElement.appendChild(addBtn);
}
