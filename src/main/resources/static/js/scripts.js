// scripts.js - Basic interactivity for ShopWear frontend
document.addEventListener('DOMContentLoaded', () => {
    // Navigation link handling
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            console.log(`Navigating to ${e.target.href}`);
            window.location.href = e.target.href; // Simulate navigation, backend handles routing
        });
    });

    // Form submission placeholder (backend handles actual submission)
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            console.log('Form submitted:', new FormData(form));
            // Backend should handle form data; this is for demo
            alert('Form submission triggered (demo). Check console for data.');
        });
    });
});