export function showLoader() {
    if (document.getElementById('global-loader')) return;

    const overlay = document.createElement('div');
    overlay.id = 'global-loader';
    overlay.style.position = 'fixed';
    overlay.style.top = 0;
    overlay.style.left = 0;
    overlay.style.width = '100vw';
    overlay.style.height = '100vh';
    overlay.style.backgroundColor = 'rgba(0, 0, 0, 0.4)';
    overlay.style.zIndex = 9999;
    overlay.style.display = 'flex';
    overlay.style.alignItems = 'center';
    overlay.style.justifyContent = 'center';

    const spinner = document.createElement('div');
    spinner.className = 'loader-spinner';

    overlay.appendChild(spinner);
    document.body.appendChild(overlay);
}

export function hideLoader() {
    const overlay = document.getElementById('global-loader');
    if (overlay) overlay.remove();
}
