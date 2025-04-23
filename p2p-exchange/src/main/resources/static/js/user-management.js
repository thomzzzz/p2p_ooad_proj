/**
 * User management functionality for the P2P Information Exchange System.
 * Handles profile viewing, editing, and user-related operations.
 */
document.addEventListener('DOMContentLoaded', function() {
    // Elements for profile page
    const profileForm = document.getElementById('profile-form');
    const changePasswordForm = document.getElementById('change-password-form');
    const profilePictureUpload = document.getElementById('profile-picture-upload');
    const profilePicturePreview = document.getElementById('profile-picture-preview');
    
    // Profile form submission
    if (profileForm) {
        profileForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            // Get form data
            const formData = new FormData(profileForm);
            const userData = {
                email: formData.get('email'),
                profileAttributes: {
                    firstName: formData.get('firstName'),
                    lastName: formData.get('lastName'),
                    bio: formData.get('bio'),
                    location: formData.get('location')
                }
            };
            
            // Update user profile
            fetch('/api/users/profile', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + localStorage.getItem('auth_token')
                },
                body: JSON.stringify(userData)
            })
            .then(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    throw new Error('Failed to update profile');
                }
            })
            .then(data => {
                showNotification('Profile updated successfully', 'success');
            })
            .catch(error => {
                showNotification('Error updating profile: ' + error.message, 'error');
            });
        });
    }
    
    // Change password form submission
    if (changePasswordForm) {
        changePasswordForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            // Get form data
            const formData = new FormData(changePasswordForm);
            const currentPassword = formData.get('currentPassword');
            const newPassword = formData.get('newPassword');
            const confirmPassword = formData.get('confirmPassword');
            
            // Validate passwords
            if (newPassword !== confirmPassword) {
                showNotification('New passwords do not match', 'error');
                return;
            }
            
            // Update password
            fetch('/api/users/password', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + localStorage.getItem('auth_token')
                },
                body: JSON.stringify({
                    currentPassword: currentPassword,
                    newPassword: newPassword
                })
            })
            .then(response => {
                if (response.ok) {
                    // Clear form
                    changePasswordForm.reset();
                    showNotification('Password updated successfully', 'success');
                } else if (response.status === 401) {
                    showNotification('Current password is incorrect', 'error');
                } else {
                    throw new Error('Failed to update password');
                }
            })
            .catch(error => {
                showNotification('Error updating password: ' + error.message, 'error');
            });
        });
    }
    
    // Profile picture upload
    if (profilePictureUpload && profilePicturePreview) {
        profilePictureUpload.addEventListener('change', function(e) {
            const file = e.target.files[0];
            
            if (file) {
                // Validate file type
                if (!file.type.startsWith('image/')) {
                    showNotification('Please select an image file', 'error');
                    return;
                }
                
                // Validate file size (max 5MB)
                if (file.size > 5 * 1024 * 1024) {
                    showNotification('Image file size must be less than 5MB', 'error');
                    return;
                }
                
                // Preview image
                const reader = new FileReader();
                reader.onload = function(event) {
                    profilePicturePreview.src = event.target.result;
                };
                reader.readAsDataURL(file);
                
                // Upload image
                const formData = new FormData();
                formData.append('profilePicture', file);
                
                fetch('/api/users/profile-picture', {
                    method: 'POST',
                    headers: {
                        'Authorization': 'Bearer ' + localStorage.getItem('auth_token')
                    },
                    body: formData
                })
                .then(response => {
                    if (response.ok) {
                        showNotification('Profile picture updated successfully', 'success');
                    } else {
                        throw new Error('Failed to update profile picture');
                    }
                })
                .catch(error => {
                    showNotification('Error updating profile picture: ' + error.message, 'error');
                });
            }
        });
    }
    
    // User dashboard stats
    const dashboardStatsContainer = document.getElementById('dashboard-stats');
    if (dashboardStatsContainer) {
        // Fetch user stats
        fetch('/api/users/stats', {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('auth_token')
            }
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Failed to fetch user stats');
            }
        })
        .then(data => {
            // Update dashboard stats
            if (data.filesCount !== undefined) {
                const filesCountElement = document.getElementById('files-count');
                if (filesCountElement) {
                    filesCountElement.textContent = data.filesCount;
                }
            }
            
            if (data.roomsCount !== undefined) {
                const roomsCountElement = document.getElementById('rooms-count');
                if (roomsCountElement) {
                    roomsCountElement.textContent = data.roomsCount;
                }
            }
            
            if (data.totalStorage !== undefined) {
                const storageUsedElement = document.getElementById('storage-used');
                if (storageUsedElement) {
                    storageUsedElement.textContent = formatFileSize(data.totalStorage);
                }
            }
            
            if (data.uploadCount !== undefined) {
                const uploadsCountElement = document.getElementById('uploads-count');
                if (uploadsCountElement) {
                    uploadsCountElement.textContent = data.uploadCount;
                }
            }
            
            if (data.downloadCount !== undefined) {
                const downloadsCountElement = document.getElementById('downloads-count');
                if (downloadsCountElement) {
                    downloadsCountElement.textContent = data.downloadCount;
                }
            }
        })
        .catch(error => {
            console.error('Error fetching user stats:', error);
        });
    }
    
    // Admin user management
    const userManagementTable = document.getElementById('user-management-table');
    if (userManagementTable) {
        // Fetch users
        fetch('/api/admin/users', {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('auth_token')
            }
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Failed to fetch users');
            }
        })
        .then(users => {
            // Clear table
            const tbody = userManagementTable.querySelector('tbody');
            tbody.innerHTML = '';
            
            // Add users to table
            users.forEach(user => {
                const row = document.createElement('tr');
                
                // Username column
                const usernameCell = document.createElement('td');
                usernameCell.textContent = user.username;
                row.appendChild(usernameCell);
                
                // Email column
                const emailCell = document.createElement('td');
                emailCell.textContent = user.email;
                row.appendChild(emailCell);
                
                // Role column
                const roleCell = document.createElement('td');
                roleCell.textContent = user.role.replace('ROLE_', '');
                row.appendChild(roleCell);
                
                // Status column
                const statusCell = document.createElement('td');
                const statusBadge = document.createElement('span');
                statusBadge.className = 'status-badge ' + (user.active ? 'active' : 'inactive');
                statusBadge.textContent = user.active ? 'Active' : 'Inactive';
                statusCell.appendChild(statusBadge);
                row.appendChild(statusCell);
                
                // Last login column
                const lastLoginCell = document.createElement('td');
                lastLoginCell.textContent = user.lastLogin ? new Date(user.lastLogin).toLocaleString() : 'Never';
                row.appendChild(lastLoginCell);
                
                // Actions column
                const actionsCell = document.createElement('td');
                
                // Edit button
                const editButton = document.createElement('button');
                editButton.className = 'action-btn edit';
                editButton.textContent = 'Edit';
                editButton.dataset.userId = user.id;
                editButton.addEventListener('click', function() {
                    openEditUserModal(user);
                });
                actionsCell.appendChild(editButton);
                
                // Toggle active button
                const toggleButton = document.createElement('button');
                toggleButton.className = 'action-btn ' + (user.active ? 'deactivate' : 'activate');
                toggleButton.textContent = user.active ? 'Deactivate' : 'Activate';
                toggleButton.dataset.userId = user.id;
                toggleButton.addEventListener('click', function() {
                    toggleUserActive(user.id, !user.active);
                });
                actionsCell.appendChild(toggleButton);
                
                row.appendChild(actionsCell);
                
                tbody.appendChild(row);
            });
        })
        .catch(error => {
            console.error('Error fetching users:', error);
        });
    }
    
    // Helper function to format file size
    function formatFileSize(sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + ' B';
        } else if (sizeInBytes < 1024 * 1024) {
            return Math.round(sizeInBytes / 1024 * 10) / 10 + ' KB';
        } else if (sizeInBytes < 1024 * 1024 * 1024) {
            return Math.round(sizeInBytes / (1024 * 1024) * 10) / 10 + ' MB';
        } else {
            return Math.round(sizeInBytes / (1024 * 1024 * 1024) * 10) / 10 + ' GB';
        }
    }
    
    // Helper function to show notifications
    function showNotification(message, type) {
        // Check if notifications container exists, create if not
        let notificationsContainer = document.getElementById('notifications-container');
        if (!notificationsContainer) {
            notificationsContainer = document.createElement('div');
            notificationsContainer.id = 'notifications-container';
            document.body.appendChild(notificationsContainer);
        }
        
        // Create notification element
        const notification = document.createElement('div');
        notification.className = 'notification ' + (type || '');
        notification.textContent = message;
        
        // Add to container
        notificationsContainer.appendChild(notification);
        
        // Auto-remove after delay
        setTimeout(() => {
            notification.classList.add('fade-out');
            setTimeout(() => {
                notificationsContainer.removeChild(notification);
            }, 500);
        }, 5000);
    }
    
    // Helper function to open edit user modal
    function openEditUserModal(user) {
        // Check if modal already exists
        let modal = document.getElementById('edit-user-modal');
        if (!modal) {
            // Create modal
            modal = document.createElement('div');
            modal.id = 'edit-user-modal';
            modal.className = 'modal';
            
            const modalContent = `
                <div class="modal-content">
                    <span class="close">&times;</span>
                    <h3>Edit User</h3>
                    <form id="edit-user-form">
                        <input type="hidden" id="edit-user-id" name="id">
                        <div class="form-group">
                            <label for="edit-username">Username</label>
                            <input type="text" id="edit-username" name="username" disabled>
                        </div>
                        <div class="form-group">
                            <label for="edit-email">Email</label>
                            <input type="email" id="edit-email" name="email" required>
                        </div>
                        <div class="form-group">
                            <label for="edit-role">Role</label>
                            <select id="edit-role" name="role" required>
                                <option value="ROLE_USER">User</option>
                                <option value="ROLE_ADMIN">Admin</option>
                            </select>
                        </div>
                        <div class="form-actions">
                            <button type="submit" class="button primary">Save Changes</button>
                            <button type="button" class="button secondary" id="cancel-edit-user">Cancel</button>
                        </div>
                    </form>
                </div>
            `;
            
            modal.innerHTML = modalContent;
            document.body.appendChild(modal);
            
            // Close button functionality
            const closeBtn = modal.querySelector('.close');
            const cancelBtn = modal.querySelector('#cancel-edit-user');
            
            closeBtn.addEventListener('click', function() {
                modal.style.display = 'none';
            });
            
            cancelBtn.addEventListener('click', function() {
                modal.style.display = 'none';
            });
            
            // Form submission
            const editUserForm = modal.querySelector('#edit-user-form');
            
            editUserForm.addEventListener('submit', function(e) {
                e.preventDefault();
                
                const formData = new FormData(editUserForm);
                const userData = {
                    id: formData.get('id'),
                    email: formData.get('email'),
                    role: formData.get('role')
                };
                
                // Update user
                fetch('/api/admin/users/' + userData.id, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': 'Bearer ' + localStorage.getItem('auth_token')
                    },
                    body: JSON.stringify(userData)
                })
                .then(response => {
                    if (response.ok) {
                        modal.style.display = 'none';
                        showNotification('User updated successfully', 'success');
                        
                        // Refresh user list
                        setTimeout(() => {
                            window.location.reload();
                        }, 1000);
                    } else {
                        throw new Error('Failed to update user');
                    }
                })
                .catch(error => {
                    showNotification('Error updating user: ' + error.message, 'error');
                });
            });
        }
        
        // Populate form with user data
        modal.querySelector('#edit-user-id').value = user.id;
        modal.querySelector('#edit-username').value = user.username;
        modal.querySelector('#edit-email').value = user.email;
        modal.querySelector('#edit-role').value = user.role;
        
        // Show modal
        modal.style.display = 'block';
    }
    
    // Helper function to toggle user active status
    function toggleUserActive(userId, active) {
        fetch('/api/admin/users/' + userId + '/' + (active ? 'activate' : 'deactivate'), {
            method: 'PUT',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('auth_token')
            }
        })
        .then(response => {
            if (response.ok) {
                showNotification('User ' + (active ? 'activated' : 'deactivated') + ' successfully', 'success');
                
                // Refresh user list
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            } else {
                throw new Error('Failed to ' + (active ? 'activate' : 'deactivate') + ' user');
            }
        })
        .catch(error => {
            showNotification('Error: ' + error.message, 'error');
        });
    }
});