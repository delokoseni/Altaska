let images = [
    '/images/coming_soon.jpg',
    '/images/coming_soon_1.jpg',
];
let currentIndex = 0;

document.addEventListener('DOMContentLoaded', function() {
    const imageContainer = document.querySelector('.image-container');
    const currentImage = document.querySelector('.current-image');
    const prevButton = document.querySelector('.prev-button');
    const nextButton = document.querySelector('.next-button');

    prevButton.addEventListener('click', function() {
        currentIndex = (currentIndex - 1 + images.length) % images.length;
        currentImage.src = images[currentIndex];
    });

    nextButton.addEventListener('click', function() {
        currentIndex = (currentIndex + 1) % images.length;
        currentImage.src = images[currentIndex];
    });
});
