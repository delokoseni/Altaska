export async function createPerformersSection(taskId, projectId, containerElement, csrfToken) {
    containerElement.innerHTML = "";

    // Создаём обёртку
    const wrapper = document.createElement("div");
    wrapper.className = "performers-section";
    containerElement.appendChild(wrapper);

    renderTitle(wrapper);

    const performers = await fetchPerformers(taskId);
    const members = await fetchMembers(projectId);

    renderPerformersList(performers, taskId, projectId, wrapper, csrfToken);
    const select = renderMemberSelect(members, wrapper);
    renderAddButton(select, taskId, projectId, wrapper, csrfToken);
}

// Заголовок
function renderTitle(containerElement) {
    const title = document.createElement("h3");
    title.textContent = "Исполнители";
    containerElement.appendChild(title);
}

// Получение исполнителей
async function fetchPerformers(taskId) {
    const response = await fetch(`/api/task-performers/${taskId}`);
    return await response.json();
}

// Получение подтверждённых участников проекта
async function fetchMembers(projectId) {
    const response = await fetch(`/api/projects/${projectId}/confirmed-members`);
    return await response.json();
}

// Отображение списка исполнителей
function renderPerformersList(performers, taskId, projectId, containerElement, csrfToken) {
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
}

// Отображение выпадающего списка участников
function renderMemberSelect(members, containerElement) {
    const select = document.createElement("select");
    members.forEach(m => {
        const opt = document.createElement("option");
        opt.value = m.userId;
        opt.textContent = m.email;
        select.appendChild(opt);
    });
    containerElement.appendChild(select);
    return select;
}

// Кнопка назначения исполнителя
function renderAddButton(select, taskId, projectId, containerElement, csrfToken) {
    const addBtn = document.createElement("button");
    addBtn.textContent = "Назначить исполнителем";
    addBtn.className = "assign-button";
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

export async function addPerformer(taskId, userId, csrfToken) {
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
        throw new Error("Ошибка: " + errorMessage);
    }
}

// Функция для взятия задачи
export function assignTask(taskId, formData, csrfToken) {
    return fetch(`/api/task-performers/${taskId}/assign`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-CSRF-TOKEN': csrfToken
        },
        body: formData
    });
}

// Функция для отказа от задачи
export function unassignTask(taskId, formData, csrfToken) {
    return fetch(`/api/task-performers/${taskId}/unassign`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-CSRF-TOKEN': csrfToken
        },
        body: formData
    });
}
