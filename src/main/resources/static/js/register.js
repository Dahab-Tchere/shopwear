$(document).ready(function() {
    const usernameInput = $('#username');
    const emailInput = $('#email');
    const passwordInput = $('#password');
    const confirmPasswordInput = $('#confirmPassword');
    const roleSelect = $('#role');
    const form = $('#registerForm');
    const csrfHeader = $('meta[name="_csrf_header"]').attr('content') || 'X-CSRF-TOKEN';
    const csrfToken = $('meta[name="_csrf"]').attr('content') || '';

    // Real-time username availability
    usernameInput.on('input', debounce(function() {
        const username = $(this).val();
        if (username.length >= 3) {
            $.ajax({
                url: '/user/check-username',
                data: { username: username },
                headers: { [csrfHeader]: csrfToken },
                success: function(isAvailable) {
                    if (isAvailable) {
                        usernameInput.removeClass('is-invalid').addClass('is-valid');
                        $('#usernameFeedback').text('');
                    } else {
                        usernameInput.removeClass('is-valid').addClass('is-invalid');
                        $('#usernameFeedback').text('Username is already taken');
                    }
                }
            });
        } else {
            usernameInput.removeClass('is-valid is-invalid');
            $('#usernameFeedback').text('');
        }
    }, 500));

    // Real-time email availability
    emailInput.on('input', debounce(function() {
        const email = $(this).val();
        if (email.match(/^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/)) {
            $.ajax({
                url: '/user/check-email',
                data: { email: email },
                headers: { [csrfHeader]: csrfToken },
                success: function(isAvailable) {
                    if (isAvailable) {
                        emailInput.removeClass('is-invalid').addClass('is-valid');
                        $('#emailFeedback').text('');
                    } else {
                        emailInput.removeClass('is-valid').addClass('is-invalid');
                        $('#emailFeedback').text('Email is already registered');
                    }
                }
            });
        } else {
            emailInput.removeClass('is-valid is-invalid');
            $('#emailFeedback').text('');
        }
    }, 500));

    // Password strength meter and validation
    passwordInput.on('input', function() {
        const password = $(this).val();
        let strength = 0;
        let feedback = [];

        if (password.length >= 8) strength += 25;
        else feedback.push('Password must be at least 8 characters');
        if (password.match(/[a-z]/)) strength += 25;
        else feedback.push('Add at least one lowercase letter');
        if (password.match(/[A-Z]/)) strength += 25;
        else feedback.push('Add at least one uppercase letter');
        if (password.match(/[0-9]/)) strength += 25;
        else feedback.push('Add at least one number');
        if (password.match(/[@$!%*?&]/)) strength += 25;
        else feedback.push('Add at least one special character (@$!%*?&)');

        const strengthBar = $('#passwordStrength');
        strengthBar.css('width', strength + '%');
        if (strength < 50) {
            strengthBar.removeClass('bg-success bg-warning').addClass('bg-danger');
            $('#passwordFeedback').text(feedback.join(', '));
            passwordInput.addClass('is-invalid');
        } else if (strength < 75) {
            strengthBar.removeClass('bg-danger bg-success').addClass('bg-warning');
            $('#passwordFeedback').text(feedback.join(', '));
            passwordInput.addClass('is-invalid');
        } else {
            strengthBar.removeClass('bg-danger bg-warning').addClass('bg-success');
            $('#passwordFeedback').text('Password is strong');
            passwordInput.removeClass('is-invalid').addClass('is-valid');
        }
    });

    // Confirm password validation
    confirmPasswordInput.on('input', function() {
        const password = passwordInput.val();
        const confirmPassword = $(this).val();
        if (password !== confirmPassword) {
            $('#confirmPasswordFeedback').text('Passwords do not match').show();
            confirmPasswordInput.addClass('is-invalid');
        } else {
            $('#confirmPasswordFeedback').text('').hide();
            confirmPasswordInput.removeClass('is-invalid').addClass('is-valid');
        }
    });

    // Form submission validation
    form.on('submit', function(e) {
        if (!form[0].checkValidity() || passwordInput.val() !== confirmPasswordInput.val()) {
            e.preventDefault();
            e.stopPropagation();
            if (passwordInput.val() !== confirmPasswordInput.val()) {
                $('#confirmPasswordFeedback').text('Passwords do not match').show();
                confirmPasswordInput.addClass('is-invalid');
            }
        }
        form.addClass('was-validated');
    });

    // Debounce function to limit AJAX calls
    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }
});