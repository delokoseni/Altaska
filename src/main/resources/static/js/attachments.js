import { showToast } from './toast.js';

// Функция для отображения блока с файлами
export function initTaskFilesSection(taskId, container, csrfToken, currentUserEmail) {
    let filesWrapper = container.querySelector('.task-files-section');

    if (!filesWrapper) {
        filesWrapper = document.createElement('div');
        filesWrapper.className = 'task-files-section';

        const title = document.createElement('h3');
        title.textContent = 'Файлы';
        title.className = 'files-title';
        title.style.textAlign = 'center';
        filesWrapper.appendChild(title);

        // Контейнер для списка файлов
        const filesList = document.createElement('div');
        filesList.className = 'files-list';
        filesWrapper.appendChild(filesList);

        // Контейнер для кнопок
        const uploadControls = document.createElement('div');
        uploadControls.className = 'upload-controls';
        uploadControls.style.marginTop = '10px';
        uploadControls.style.textAlign = 'center';

        const fileInput = document.createElement('input');
        fileInput.type = 'file';
        fileInput.id = 'fileInput';
        fileInput.style.color = 'black';
        uploadControls.appendChild(fileInput);

        const uploadButton = document.createElement('button');
        uploadButton.textContent = 'Загрузить файл';
        uploadButton.style.marginLeft = '10px';
        uploadButton.addEventListener('click', () => {
            const file = fileInput.files[0];
            if (file) {
                uploadFile(file, taskId, filesList, csrfToken, currentUserEmail);
            } else {
                alert('Пожалуйста, выберите файл');
            }
        });
        uploadControls.appendChild(uploadButton);

        filesWrapper.appendChild(uploadControls);

        container.appendChild(filesWrapper);
    }

    loadFilesList(taskId, filesWrapper.querySelector('.files-list'), csrfToken, currentUserEmail);

}

// Функция для загрузки списка файлов
export function loadFilesList(taskId, filesListContainer, csrfToken, currentUserEmail) {
    filesListContainer.innerHTML = '';

    fetch(`/api/files/task/${taskId}`)
        .then(response => {
            if (!response.ok) throw new Error('Ошибка загрузки списка файлов');
            return response.json();
        })
        .then(files => {
            if (!Array.isArray(files) || files.length === 0) {
                const noFiles = document.createElement('p');
                noFiles.textContent = 'Нет прикрепленных файлов';
                filesListContainer.appendChild(noFiles);
                return;
            }

            files.forEach(file => {
                const fileItem = document.createElement('div');
                fileItem.className = 'file-item';
                const fileName = document.createElement('div');
                fileName.className = 'file-name';
                fileName.textContent = file.fileName;
                fileItem.appendChild(fileName);

                const downloadIcon = document.createElement('img');
                downloadIcon.src = '/icons/download.svg';
                downloadIcon.alt = 'Скачать';
                downloadIcon.classList.add('download-icon');
                downloadIcon.addEventListener('click', () => {
                    window.location.href = `/api/files/download/${file.id}`;
                });

                const iconsContainer = document.createElement('div');
                iconsContainer.className = 'file-icons';
                if (file.uploadedByEmail === currentUserEmail) {
                    const deleteIcon = document.createElement('img');
                    deleteIcon.src = '/icons/trash.svg'; // Замените на путь к вашей SVG-иконке
                    deleteIcon.alt = 'Удалить';
                    deleteIcon.classList.add('delete-icon'); // Добавляем класс для стилизации
                    iconsContainer.appendChild(deleteIcon);
                    deleteIcon.addEventListener('click', () => {
                        deleteFile(file.id, taskId, filesListContainer, csrfToken, currentUserEmail);
                    });

                }

                iconsContainer.appendChild(downloadIcon);

                fileItem.appendChild(iconsContainer);
                filesListContainer.appendChild(fileItem);
            });
        })
        .catch(error => {
            console.error('Ошибка при загрузке файлов:', error);
            const errorMsg = document.createElement('p');
            errorMsg.textContent = 'Не удалось загрузить список файлов';
            filesListContainer.appendChild(errorMsg);
        });
}

// Функция для отправки файла на сервер
export function uploadFile(file, taskId, filesListContainer, csrfToken, currentUserEmail) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('taskId', taskId);

    fetch('/api/files/upload', {
        method: 'POST',
        headers: {
            'X-CSRF-TOKEN': csrfToken
        },
        body: formData
    })
    .then(async response => {
        if (!response.ok) {
            const errorText = await response.text();
            const hasEnglish = /[a-zA-Z]/.test(errorText);
            if (hasEnglish) {
                showToast("Не удалось загрузить файл: неизвестная ошибка", "error");
            } else {
                showToast("Не удалось загрузить файл: " + errorText, "error");
            }
            throw new Error(errorText);
        }
        return response.json();
    })
    .then(() => {
        loadFilesList(taskId, filesListContainer, csrfToken, currentUserEmail);
        const fileInput = document.getElementById('fileInput');
        const fileLabel = document.getElementById('fileLabel');
        if (fileInput) fileInput.value = '';
        if (fileLabel) fileLabel.textContent = 'Файл не выбран';
    })
    .catch(error => {
        const message = error.message || '';
        const hasEnglish = /[a-zA-Z]/.test(message);
        if (hasEnglish) {
            if (message === 'Failed to fetch') {
                showToast("Не удалось загрузить файл: проверьте подключение к интернету или попробуйте позже", "error");
            } else {
                showToast("Не удалось загрузить файл: неизвестная ошибка", "error");
            }
        } else {
            showToast("Не удалось загрузить файл: " + message, "error");
        }
        console.error("Ошибка при загрузке файла:", error);
    });
}

function deleteFile(fileId, taskId, filesListContainer, csrfToken, currentUserEmail) {
    fetch(`/api/files/${fileId}`, {
        method: 'DELETE',
        headers: {
            'X-CSRF-TOKEN': csrfToken
        }
    })
    .then(async response => {
        if (!response.ok) {
            const errorText = await response.text();
            const hasEnglish = /[a-zA-Z]/.test(errorText);
            if (hasEnglish) {
                showToast("Не удалось удалить файл: неизвестная ошибка", "error");
            } else {
                showToast("Не удалось удалить файл: " + errorText, "error");
            }
            throw new Error(errorText);
        }
        return response.text();
    })
    .then(() => {
        loadFilesList(taskId, filesListContainer, csrfToken, currentUserEmail);
    })
    .catch(error => {
        const message = error.message || '';
        const hasEnglish = /[a-zA-Z]/.test(message);
        if (hasEnglish) {
            if (message === 'Failed to fetch') {
                showToast("Не удалось удалить файл: проверьте подключение к интернету или попробуйте позже", "error");
            } else {
                showToast("Не удалось удалить файл: неизвестная ошибка", "error");
            }
        } else {
            showToast("Не удалось удалить файл: " + message, "error");
        }
        console.error("Ошибка при удалении файла:", error);
    });
}



