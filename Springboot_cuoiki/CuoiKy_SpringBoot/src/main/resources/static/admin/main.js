function clearFilters() {
    // Xóa tất cả các radio button đã chọn
    document.querySelectorAll('input[type="radio"]').forEach(radio => {
        radio.checked = false;
    });
    
    // Reset trang về 0 và submit form
    document.querySelector('input[name="page"]').value = '0';
    document.getElementById('filterForm').submit();
}

function addToCart(productId) {
    fetch('/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ productId: productId, quantity: 1 })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Cập nhật số lượng giỏ hàng trên navbar
            document.querySelector('.badge').textContent = data.cartCount;
            alert('Đã thêm sản phẩm vào giỏ hàng!');
        } else {
            alert('Có lỗi xảy ra khi thêm vào giỏ hàng!');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Có lỗi xảy ra khi thêm vào giỏ hàng!');
    });
}

// Khởi tạo các dropdown menus và tooltips khi trang được tải
document.addEventListener('DOMContentLoaded', function() {
    // Khởi tạo tất cả các dropdown menus
    var dropdownElementList = [].slice.call(document.querySelectorAll('.dropdown-toggle'));
    var dropdownList = dropdownElementList.map(function(dropdownToggleEl) {
        return new bootstrap.Dropdown(dropdownToggleEl);
    });
    
    // Khởi tạo tooltips nếu có
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Tự động hiển thị dropdown khi bấm vào biểu tượng người dùng
    var userDropdown = document.getElementById('userDropdown');
    if (userDropdown) {
        // Tạo đối tượng dropdown từ Bootstrap
        var dropdown = bootstrap.Dropdown.getInstance(userDropdown);
        if (!dropdown) {
            dropdown = new bootstrap.Dropdown(userDropdown);
        }
        
        // Thêm sự kiện click để tự động hiển thị dropdown
        userDropdown.addEventListener('click', function(e) {
            e.preventDefault();
            dropdown.show();
        });
        
        // Thêm sự kiện cho các phần tử con trong dropdown
        var dropdownItems = document.querySelectorAll('.dropdown-item');
        dropdownItems.forEach(function(item) {
            item.addEventListener('click', function(e) {
                // Nếu là button đăng xuất, không ngăn chặn hành vi mặc định
                if (!this.closest('form')) {
                    e.stopPropagation();
                }
            });
        });
    }
    
    // Log để debug
    console.log('Bootstrap components initialized');
    console.log('Dropdowns found:', dropdownElementList.length);
}); 