// HMS Application Entry Point

const HMS_APP = {
    routes: {
        'dashboard': (typeof renderDashboardPage === 'function' ? renderDashboardPage : null),
        'triage': () => window.HMS.ui.toast('Chức năng AI Phân luồng đang phát triển', 'info'),
        'queue': () => window.HMS.ui.toast('Chức năng Hàng đợi đang phát triển', 'info'),
        'appointments': () => window.HMS.ui.toast('Chức năng Lịch hẹn đang phát triển', 'info'),
        'patients': () => window.HMS.ui.toast('Chức năng Bệnh nhân đang phát triển', 'info'),
        'doctors': () => window.HMS.ui.toast('Chức năng Bác sĩ đang phát triển', 'info'),
        'pharmacy': () => window.HMS.ui.toast('Chức năng Kho thuốc đang phát triển', 'info'),
        'labs': () => window.HMS.ui.toast('Chức năng Xét nghiệm đang phát triển', 'info'),
        'settings': () => window.HMS.ui.toast('Chức năng Cài đặt đang phát triển', 'info')
    },
    
    init() {
        this.setupLayout();
        this.setupRouter();
        this.setupTheme();
        
        // Initialize lucide icons for initial layout elements
        if (window.lucide) {
            lucide.createIcons();
        }
        
        // Trigger initial route
        this.handleRouteChange();
    },

    setupLayout() {
        const sidebar = document.getElementById('sidebar');
        const overlay = document.getElementById('sidebar-overlay');
        const toggleBtn = document.getElementById('sidebar-toggle-btn');
        const closeBtn = document.getElementById('sidebar-close-btn');

        const toggleSidebar = () => {
            if (sidebar) sidebar.classList.toggle('open');
            if (overlay) overlay.classList.toggle('active');
        };

        const closeSidebar = () => {
            if (sidebar) sidebar.classList.remove('open');
            if (overlay) overlay.classList.remove('active');
        };

        toggleBtn?.addEventListener('click', toggleSidebar);
        closeBtn?.addEventListener('click', closeSidebar);
        overlay?.addEventListener('click', closeSidebar);

        // Close sidebar on mobile when navigating
        document.querySelectorAll('.sb-link').forEach(item => {
            item.addEventListener('click', () => {
                if (window.innerWidth <= 992) {
                    closeSidebar();
                }
            });
        });
    },

    setupRouter() {
        window.addEventListener('hashchange', () => this.handleRouteChange());
    },

    handleRouteChange() {
        const hash = window.location.hash.slice(2) || 'dashboard'; // e.g. #/dashboard -> dashboard
        const appContent = document.getElementById('app-content');
        if (!appContent) return;

        // Update nav active state
        document.querySelectorAll('.sb-link').forEach(item => {
            item.classList.remove('active');
            if (item.getAttribute('data-route') === hash) {
                item.classList.add('active');
            }
        });

        // Simple routing logic
        if (this.routes[hash]) {
            const result = this.routes[hash](appContent);
            if (typeof result === 'string') {
                appContent.innerHTML = result;
                if (window.lucide) {
                    lucide.createIcons({ root: appContent });
                }
                // Trigger an event for pages to initialize their JS (charts, etc.)
                document.dispatchEvent(new CustomEvent(`hms:page:${hash}:loaded`, { detail: { container: appContent } }));
            }
        } else {
            appContent.innerHTML = `
                <div class="empty-state" style="padding: 40px 0; text-align: center;">
                    <i data-lucide="file-question" style="width: 48px; height: 48px; margin-bottom: 16px; color: var(--text-3);"></i>
                    <h3>Không tìm thấy trang</h3>
                    <p class="text-muted">Trang bạn yêu cầu không tồn tại hoặc đang được phát triển.</p>
                </div>
            `;
            if (window.lucide) {
                lucide.createIcons({ root: appContent });
            }
        }
    },
    
    setupTheme() {
        const themeToggle = document.getElementById('theme-toggle');
        const html = document.documentElement;
        
        // Check saved theme
        const savedTheme = localStorage.getItem('hms_theme') || 'light';
        html.setAttribute('data-theme', savedTheme);
        this.updateThemeIcon(savedTheme);
        
        themeToggle?.addEventListener('click', () => {
            const currentTheme = html.getAttribute('data-theme');
            const newTheme = currentTheme === 'light' ? 'dark' : 'light';
            
            html.setAttribute('data-theme', newTheme);
            localStorage.setItem('hms_theme', newTheme);
            this.updateThemeIcon(newTheme);
            
            // Re-render charts for theme update if needed
            document.dispatchEvent(new CustomEvent('hms:theme:changed', { detail: { theme: newTheme } }));
        });
    },
    
    updateThemeIcon(theme) {
        const moon = document.querySelector('.dark-icon');
        const sun = document.querySelector('.light-icon');
        if (!moon || !sun) return;
        if (theme === 'dark') {
            moon.style.display = 'none';
            sun.style.display = 'block';
        } else {
            moon.style.display = 'block';
            sun.style.display = 'none';
        }
    }
};

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    // Wait slightly to ensure data is loaded if it's large, or just init
    setTimeout(() => {
        HMS_APP.init();
    }, 0);
});
