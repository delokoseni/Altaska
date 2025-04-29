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
                fileItem.textContent = `${file.fileName} (${file.fileType}) `;

                const downloadButton = document.createElement('button');
                downloadButton.textContent = 'Скачать';
                downloadButton.addEventListener('click', () => {
                    window.location.href = `/api/files/download/${file.id}`;
                });
                fileItem.appendChild(downloadButton);

                if (file.uploadedByEmail === currentUserEmail) {
                    const deleteButton = document.createElement('button');
                    deleteButton.textContent = 'Удалить';
                    deleteButton.style.marginLeft = '10px';
                    deleteButton.addEventListener('click', () => {
                        deleteFile(file.id, taskId, filesListContainer, csrfToken, currentUserEmail);
                    });
                    fileItem.appendChild(deleteButton);
                }

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
    .then(response => {
        if (!response.ok) {
            throw new Error('Ошибка при загрузке файла');
        }
        return response.json();
    })
    .then(data => {
        console.log('Файл загружен успешно:', data);
        loadFilesList(taskId, filesListContainer, csrfToken, currentUserEmail);
    })
    .catch(error => {
        console.error('Ошибка при загрузке файла:', error);
        alert('Не удалось загрузить файл');
    });
}

function deleteFile(fileId, taskId, filesListContainer, csrfToken, currentUserEmail) {
    fetch(`/api/files/${fileId}`, {
        method: 'DELETE',
        headers: {
            'X-CSRF-TOKEN': csrfToken
        }
    })
    .then(response => {
        if (!response.ok) throw new Error('Ошибка при удалении файла');
        return response.text();
    })
    .then(() => {
        console.log('Файл удалён успешно');
        loadFilesList(taskId, filesListContainer, csrfToken, currentUserEmail);
    })
    .catch(error => {
        console.error('Ошибка при удалении файла:', error);
        alert('Не удалось удалить файл');
    });
}


