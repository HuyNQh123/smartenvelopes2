/** Tương tác trình bày Member 1; không thay đổi dữ liệu hoặc kiểm tra nghiệp vụ. */
document.addEventListener('DOMContentLoaded', function () {
    // Chỉ dịch thông báo ràng buộc HTML đã có, không thay đổi quy tắc validation.
    document.querySelectorAll('.auth-page form input, .member-page form input, .member-page form select, .member-page form textarea').forEach(function (input) {
        input.addEventListener('input', function () {
            if (input.type === 'radio' && input.form) {
                Array.from(input.form.elements).filter(item => item.type === 'radio' && item.name === input.name)
                    .forEach(item => item.setCustomValidity(''));
            } else input.setCustomValidity('');
        });
        input.addEventListener('invalid', function () {
            const validity = input.validity;
            if (validity.valueMissing) input.setCustomValidity('Vui lòng hoàn thành trường này.');
            else if (validity.typeMismatch && input.type === 'email') input.setCustomValidity('Vui lòng nhập địa chỉ email hợp lệ.');
            else if (validity.tooShort) input.setCustomValidity('Vui lòng nhập ít nhất ' + input.minLength + ' ký tự.');
            else if (validity.tooLong) input.setCustomValidity('Vui lòng nhập tối đa ' + input.maxLength + ' ký tự.');
        });
    });
    document.querySelectorAll('[data-password-toggle]').forEach(function (button) {
        button.addEventListener('click', function () {
            const input = document.getElementById(button.dataset.passwordToggle);
            if (!input) return;
            const visible = input.type === 'password';
            input.type = visible ? 'text' : 'password';
            button.setAttribute('aria-pressed', String(visible));
            button.setAttribute('aria-label', visible ? 'Ẩn mật khẩu' : 'Hiện mật khẩu');
            const icon = button.querySelector('.bi');
            if (icon) { icon.classList.toggle('bi-eye', !visible); icon.classList.toggle('bi-eye-slash', visible); }
        });
    });

    const busyButtons = new Map();
    document.querySelectorAll('form[data-submit-feedback]').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (event.defaultPrevented) return;
            const button = event.submitter || form.querySelector('[data-loading-button]');
            // Không disable submitter có name vì giá trị đó có thể là một phần hợp đồng POST.
            if (!button || button.name || !button.hasAttribute('data-loading-button')) return;
            busyButtons.set(button, button.getAttribute('aria-label'));
            button.classList.add('is-loading');
            button.setAttribute('aria-label', 'Đang xử lý');
            button.setAttribute('aria-busy', 'true');
            button.disabled = true;
        });
    });
    window.addEventListener('pageshow', function () {
        busyButtons.forEach(function (label, button) {
            button.disabled = false;
            button.classList.remove('is-loading');
            button.removeAttribute('aria-busy');
            if (label === null) button.removeAttribute('aria-label');
            else button.setAttribute('aria-label', label);
        });
        busyButtons.clear();
    });

    document.querySelectorAll('[data-color-picker]').forEach(function (picker) {
        picker.addEventListener('input', function () {
            const text = document.getElementById(picker.dataset.colorPicker);
            if (text) text.value = picker.value;
        });
    });
    document.querySelectorAll('[data-color-text]').forEach(function (text) {
        text.addEventListener('input', function () {
            const picker = document.getElementById(text.dataset.colorText);
            if (picker && /^#[0-9a-f]{6}$/i.test(text.value)) picker.value = text.value;
        });
    });
});
