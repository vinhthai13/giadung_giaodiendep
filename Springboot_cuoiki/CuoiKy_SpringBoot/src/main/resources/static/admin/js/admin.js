// Admin Dashboard JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Set animation order for cards
    const cards = document.querySelectorAll('.card');
    cards.forEach((card, index) => {
        card.style.setProperty('--animation-order', index);
    });
    
    // Apply animation classes to elements
    const contentElements = document.querySelectorAll('.content > *:not(.card)');
    contentElements.forEach((el, index) => {
        el.classList.add('fade-in');
        el.style.animationDelay = (index * 0.1) + 's';
    });
    
    // Add slide-in animation to sidebar links
    const sidebarLinks = document.querySelectorAll('.sidebar .nav-link');
    sidebarLinks.forEach((link, index) => {
        link.classList.add('slide-in');
        link.style.animationDelay = (index * 0.05) + 's';
        
        // Handle click event to toggle icon visibility
        link.addEventListener('click', function(e) {
            // For dropdown toggles, don't hide the icon
            if (this.classList.contains('dropdown-toggle')) {
                return;
            }
            
            // Check if this link is not within a dropdown or is a direct menu item
            if (!this.closest('.collapse') || this.classList.contains('active')) {
                // First reset all menu items
                document.querySelectorAll('.sidebar .nav-link').forEach(menuItem => {
                    menuItem.classList.remove('menu-active');
                });
                
                // Add active class to hide icon
                this.classList.add('menu-active');
                
                // Store active menu in sessionStorage
                sessionStorage.setItem('activeMenu', this.getAttribute('href'));
            }
        });
    });
    
    // Check if there's a stored active menu on page load
    const activeMenuHref = sessionStorage.getItem('activeMenu');
    if (activeMenuHref) {
        const activeMenuItem = document.querySelector(`.sidebar .nav-link[href="${activeMenuHref}"]`);
        if (activeMenuItem) {
            activeMenuItem.classList.add('menu-active');
        }
    }
    
    // Set active class for current page
    const currentPath = window.location.pathname;
    document.querySelectorAll('.sidebar .nav-link').forEach(link => {
        const href = link.getAttribute('href');
        if (href && currentPath.endsWith(href)) {
            link.classList.add('menu-active');
        }
    });
    
    // Auto expand dropdown menu if a child item is active
    const activeLinks = document.querySelectorAll('.sidebar .collapse .nav-link.active');
    activeLinks.forEach(function(link) {
        const parentCollapse = link.closest('.collapse');
        if (parentCollapse) {
            parentCollapse.classList.add('show');
            const parentToggle = document.querySelector('[data-bs-target="#' + parentCollapse.id + '"]');
            if (parentToggle) {
                parentToggle.classList.add('active');
                parentToggle.setAttribute('aria-expanded', 'true');
            }
        }
    });
    
    // Table row hover effect enhancement
    const tableRows = document.querySelectorAll('tbody tr');
    tableRows.forEach(row => {
        row.addEventListener('mouseenter', () => {
            const cells = row.querySelectorAll('td');
            cells.forEach((cell, index) => {
                cell.style.transition = 'transform 0.3s ease';
                cell.style.transitionDelay = (index * 0.03) + 's';
                cell.style.transform = 'translateY(-2px)';
            });
        });
        
        row.addEventListener('mouseleave', () => {
            const cells = row.querySelectorAll('td');
            cells.forEach(cell => {
                cell.style.transform = 'translateY(0)';
            });
        });
    });
    
    // Add pulsate effect to primary action buttons
    const primaryActionButtons = document.querySelectorAll('.btn-primary[data-action="primary"]');
    primaryActionButtons.forEach(button => {
        button.classList.add('pulsate');
    });
    
    // Subtle parallax effect for cards
    const handleCardParallax = () => {
        cards.forEach(card => {
            card.addEventListener('mousemove', (e) => {
                const rect = card.getBoundingClientRect();
                const x = e.clientX - rect.left;
                const y = e.clientY - rect.top;
                
                const xPercent = ((x / rect.width) - 0.5) * 10;
                const yPercent = ((y / rect.height) - 0.5) * 10;
                
                card.style.transform = `translateY(-10px) rotateX(${-yPercent}deg) rotateY(${xPercent}deg)`;
                card.style.boxShadow = `${xPercent/2}px ${yPercent/2}px 20px rgba(0,0,0,0.2)`;
            });
            
            card.addEventListener('mouseleave', () => {
                card.style.transform = 'translateY(-10px) rotateX(0) rotateY(0)';
                card.style.boxShadow = 'var(--hover-shadow)';
                card.style.transition = 'transform 0.5s ease, box-shadow 0.5s ease';
            });
        });
    };
    
    // Only apply parallax on larger screens
    if (window.innerWidth > 768) {
        handleCardParallax();
    }
    
    // Smooth scrolling for anchors
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            e.preventDefault();
            
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
    
    // Animate numbers in statistics
    const animateValue = (element, start, end, duration) => {
        if (!element) return;
        
        let startTimestamp = null;
        const step = (timestamp) => {
            if (!startTimestamp) startTimestamp = timestamp;
            const progress = Math.min((timestamp - startTimestamp) / duration, 1);
            const value = Math.floor(progress * (end - start) + start);
            element.textContent = value.toLocaleString();
            if (progress < 1) {
                window.requestAnimationFrame(step);
            }
        };
        window.requestAnimationFrame(step);
    };
    
    // Add intersection observer for animated stats
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const element = entry.target;
                const finalValue = parseInt(element.getAttribute('data-value') || '0');
                animateValue(element, 0, finalValue, 1500);
                observer.unobserve(element);
            }
        });
    }, { threshold: 0.2 });
    
    // Observe all stat number elements
    document.querySelectorAll('.stat-number').forEach(el => observer.observe(el));
}); 