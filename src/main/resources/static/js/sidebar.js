window.addEventListener('resize', updateSidebarHeight);
window.addEventListener('scroll', updateSidebarHeight);

function updateSidebarHeight() {
    const sidebar = document.querySelector('.sidebar');
    const pageHeight = document.documentElement.scrollHeight;
    const windowHeight = window.innerHeight;
    sidebar.style.height = `${Math.max(pageHeight, windowHeight)}px`;
}

updateSidebarHeight();
