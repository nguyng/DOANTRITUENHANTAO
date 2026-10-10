// Dashboard Page Controller

function renderDashboardPage(container) {
    // Wait for data to be available
    if (!window.HMS_DATA || !window.HMS) {
        return `<div class="page-head"><h2>Loading...</h2></div>`;
    }
    
    const data = window.HMS_DATA;
    const HMS = window.HMS;
    
    // Quick Stats mock logic
    const todayAppointments = data.appointments.filter(a => HMS.formatDate(a.time, 'yyyy-MM-dd') === HMS.formatDate(new Date(), 'yyyy-MM-dd'));
    const activeAppointments = todayAppointments.filter(a => ['scheduled', 'in_progress'].includes(a.status)).length;
    const todayPatients = 245; // Mock static for demo
    
    const html = `
        <div class="page-head">
            <div>
                <h1>Tổng quan</h1>
                <p class="sub">Chào mừng trở lại, Dr. Nguyễn Văn A. Dưới đây là tình hình bệnh viện hôm nay.</p>
            </div>
            <div class="page-actions">
                <button class="btn btn-outline">
                    <i data-lucide="calendar"></i>
                    ${HMS.formatDate(new Date(), 'dd/MM/yyyy')}
                </button>
                <button class="btn btn-primary">
                    <i data-lucide="plus"></i>
                    Bệnh nhân mới
                </button>
            </div>
        </div>

        <div class="grid-cards cols-4" style="margin-bottom: 24px;">
            <div class="card card-hover">
                <div class="card-body">
                    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px;">
                        <div>
                            <div class="text-sm" style="color:var(--text-3); margin-bottom: 4px;">Lượt khám hôm nay</div>
                            <div style="font-size: 24px; font-weight: 700; color: var(--text-primary);">${todayPatients}</div>
                        </div>
                        <div class="ichip" style="background:var(--tint-primary); color:var(--primary);">
                            <i data-lucide="users"></i>
                        </div>
                    </div>
                    <div class="text-sm" style="color: var(--success-500); display: flex; align-items: center; gap: 4px;">
                        <i data-lucide="trending-up" style="width: 14px; height: 14px;"></i>
                        <span>+12% so với hôm qua</span>
                    </div>
                </div>
            </div>
            
            <div class="card card-hover">
                <div class="card-body">
                    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px;">
                        <div>
                            <div class="text-sm" style="color:var(--text-3); margin-bottom: 4px;">Lịch hẹn hôm nay</div>
                            <div style="font-size: 24px; font-weight: 700; color: var(--text-primary);">${todayAppointments.length}</div>
                        </div>
                        <div class="ichip" style="background:var(--tint-warning); color:var(--warning-500);">
                            <i data-lucide="calendar-clock"></i>
                        </div>
                    </div>
                    <div class="text-sm" style="color: var(--text-3); display: flex; align-items: center; gap: 4px;">
                        <span>Đang chờ: ${activeAppointments}</span>
                    </div>
                </div>
            </div>
            
            <div class="card card-hover">
                <div class="card-body">
                    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px;">
                        <div>
                            <div class="text-sm" style="color:var(--text-3); margin-bottom: 4px;">Ca cấp cứu (AI)</div>
                            <div style="font-size: 24px; font-weight: 700; color: var(--text-primary);">8</div>
                        </div>
                        <div class="ichip" style="background:var(--tint-danger); color:var(--danger-500);">
                            <i data-lucide="activity"></i>
                        </div>
                    </div>
                    <div class="text-sm" style="color: var(--danger-500); display: flex; align-items: center; gap: 4px;">
                        <i data-lucide="alert-circle" style="width: 14px; height: 14px;"></i>
                        <span>Cần xử lý ngay: 2 ca</span>
                    </div>
                </div>
            </div>

            <div class="card card-hover">
                <div class="card-body">
                    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px;">
                        <div>
                            <div class="text-sm" style="color:var(--text-3); margin-bottom: 4px;">Phòng bệnh trống</div>
                            <div style="font-size: 24px; font-weight: 700; color: var(--text-primary);">42 / 250</div>
                        </div>
                        <div class="ichip" style="background:var(--tint-success); color:var(--success-500);">
                            <i data-lucide="bed"></i>
                        </div>
                    </div>
                    <div class="text-sm" style="color: var(--success-500); display: flex; align-items: center; gap: 4px;">
                        <span>Công suất: 83.2%</span>
                    </div>
                </div>
            </div>
        </div>

        <div class="grid-cards cols-2">
            <div class="card">
                <div class="card-head">
                    <h3 class="card-title">Thống kê lượt khám (7 ngày qua)</h3>
                </div>
                <div class="card-body">
                    <div id="visit-chart" style="min-height: 300px;"></div>
                </div>
            </div>
            
            <div class="card">
                <div class="card-head">
                    <h3 class="card-title">Lịch hẹn sắp tới</h3>
                    <a href="#/appointments" style="color: var(--primary); font-size: 13px; font-weight: 500;">Xem tất cả</a>
                </div>
                <div class="card-body" style="padding: 0; margin-top: 12px;">
                    <div class="table-wrap">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th>Bệnh nhân</th>
                                    <th>Thời gian</th>
                                    <th>Bác sĩ</th>
                                    <th>Trạng thái</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${data.appointments.slice(0, 6).map(app => {
                                    const patient = data.patients.find(p => p.id === app.patientId) || {};
                                    const doctor = data.doctors.find(d => d.id === app.doctorId) || {};
                                    const statusMeta = HMS.getStatusMeta(app.status, 'appointment');
                                    let badgeColor = 'primary';
                                    if(statusMeta.badgeClass.includes('warning')) badgeColor = 'warning';
                                    if(statusMeta.badgeClass.includes('success')) badgeColor = 'success';
                                    if(statusMeta.badgeClass.includes('danger')) badgeColor = 'danger';
                                    return `
                                        <tr>
                                            <td>
                                                <div style="font-weight: 500;">${patient.fullName || 'Unknown'}</div>
                                                <div style="font-size: 12px; color: var(--text-3);">${patient.id || ''}</div>
                                            </td>
                                            <td>
                                                <div style="font-weight: 500;">${HMS.formatDate(app.time, 'HH:mm')}</div>
                                                <div style="font-size: 12px; color: var(--text-3);">${HMS.formatDate(app.time, 'dd/MM/yyyy')}</div>
                                            </td>
                                            <td>${doctor.fullName || 'N/A'}</td>
                                            <td>
                                                <span class="badge" style="background:var(--tint-${badgeColor}); color:var(--${badgeColor}-500);">
                                                    ${statusMeta.label}
                                                </span>
                                            </td>
                                        </tr>
                                    `;
                                }).join('')}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    `;

    return html;
}

// Chart Instance reference
let dashboardVisitChart = null;

// Initialize Charts when dashboard is loaded
document.addEventListener('hms:page:dashboard:loaded', () => {
    
    if (typeof ApexCharts === 'undefined') return;

    // Destroy existing chart if re-rendering
    if (dashboardVisitChart) {
        dashboardVisitChart.destroy();
    }

    const chartOptions = {
        series: [{
            name: 'Khám nội trú',
            data: [31, 40, 28, 51, 42, 109, 100]
        }, {
            name: 'Khám ngoại trú',
            data: [11, 32, 45, 32, 34, 52, 41]
        }],
        chart: {
            height: 320,
            type: 'area',
            fontFamily: 'Inter, sans-serif',
            toolbar: {
                show: false
            },
            animations: {
                enabled: true,
                easing: 'easeinout',
                speed: 800
            }
        },
        colors: ['#0ea5e9', '#10b981'],
        dataLabels: {
            enabled: false
        },
        stroke: {
            curve: 'smooth',
            width: 2
        },
        xaxis: {
            categories: ['Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7', 'CN'],
            axisBorder: { show: false },
            axisTicks: { show: false },
            labels: {
                style: {
                    colors: 'var(--text-muted)'
                }
            }
        },
        yaxis: {
            labels: {
                style: {
                    colors: 'var(--text-muted)'
                }
            }
        },
        grid: {
            borderColor: 'var(--border-color)',
            strokeDashArray: 4,
            yaxis: {
                lines: { show: true }
            },
            padding: {
                top: 0,
                right: 0,
                bottom: 0,
                left: 10
            }
        },
        legend: {
            position: 'top',
            horizontalAlign: 'right',
            labels: {
                colors: 'var(--text-secondary)'
            }
        },
        theme: {
            mode: document.documentElement.getAttribute('data-theme') || 'light'
        },
        fill: {
            type: 'gradient',
            gradient: {
                shadeIntensity: 1,
                opacityFrom: 0.4,
                opacityTo: 0.05,
                stops: [0, 90, 100]
            }
        },
        tooltip: {
            theme: document.documentElement.getAttribute('data-theme') || 'light'
        }
    };

    const chartEl = document.querySelector("#visit-chart");
    if (chartEl) {
        dashboardVisitChart = new ApexCharts(chartEl, chartOptions);
        dashboardVisitChart.render();
    }
});

// Update chart theme on theme toggle
document.addEventListener('hms:theme:changed', (e) => {
    if (dashboardVisitChart) {
        dashboardVisitChart.updateOptions({
            theme: { mode: e.detail.theme },
            tooltip: { theme: e.detail.theme },
            xaxis: { labels: { style: { colors: 'var(--text-muted)' } } },
            yaxis: { labels: { style: { colors: 'var(--text-muted)' } } },
            legend: { labels: { colors: 'var(--text-secondary)' } }
        });
    }
});
