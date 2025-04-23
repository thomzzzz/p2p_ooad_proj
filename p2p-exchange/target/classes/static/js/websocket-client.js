document.addEventListener('DOMContentLoaded', function() {
    // WebSocket connection for real-time updates
    let socket;
    
    function connectWebSocket() {
        // Check if we're on a room page
        const isRoomPage = window.location.pathname.includes('/rooms/') && 
                         !window.location.pathname.includes('/join/');
        
        // Get room ID from URL if we're on a room page
        let roomId = null;
        if (isRoomPage) {
            const urlParts = window.location.pathname.split('/');
            roomId = urlParts[urlParts.length - 1];
        }
        
        // Connect to WebSocket
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${protocol}//${window.location.host}/ws`;
        
        socket = new WebSocket(wsUrl);
        
        socket.onopen = function() {
            console.log('WebSocket connection established');
            
            // If we're in a room, subscribe to room events
            if (roomId) {
                const subscribeMsg = {
                    action: 'SUBSCRIBE',
                    roomId: roomId
                };
                socket.send(JSON.stringify(subscribeMsg));
            }
        };
        
        socket.onmessage = function(event) {
            const message = JSON.parse(event.data);
            
            // Handle different types of events
            switch (message.type) {
                case 'USER_JOINED':
                    handleUserJoined(message.data);
                    break;
                case 'USER_LEFT':
                    handleUserLeft(message.data);
                    break;
                case 'FILE_SHARED':
                    handleFileShared(message.data);
                    break;
                case 'FILE_DOWNLOADED':
                    handleFileDownloaded(message.data);
                    break;
                case 'MESSAGE':
                    handleChatMessage(message.data);
                    break;
            }
        };
        
        socket.onclose = function() {
            console.log('WebSocket connection closed');
            // Try to reconnect after a delay
            setTimeout(connectWebSocket, 5000);
        };
        
        socket.onerror = function(error) {
            console.error('WebSocket error:', error);
        };
    }
    
    // Handle different event types
    function handleUserJoined(data) {
        const { username, userId } = data;
        
        // Add user to members list if not already there
        const membersList = document.querySelector('.members-list');
        if (membersList) {
            // Check if user is already in the list
            const existingMember = document.querySelector(`.member-item[data-id="${userId}"]`);
            if (!existingMember) {
                // Create new member element
                const memberHTML = `
                    <div class="member-item" data-id="${userId}">
                        <div class="member-avatar">
                            <img src="/img/default-avatar.png" alt="User Avatar">
                            <span class="status-indicator online"></span>
                        </div>
                        <div class="member-details">
                            <h4>${username}</h4>
                            <p class="member-status">Online</p>
                        </div>
                    </div>
                `;
                membersList.insertAdjacentHTML('beforeend', memberHTML);
            } else {
                // Update status to online
                const statusIndicator = existingMember.querySelector('.status-indicator');
                statusIndicator.classList.remove('offline');
                statusIndicator.classList.add('online');
                
                const statusText = existingMember.querySelector('.member-status');
                statusText.textContent = 'Online';
            }
        }
        
        // Show notification
        showNotification(`${username} joined the room`);
    }
    
    function handleUserLeft(data) {
        const { username, userId } = data;
        
        // Update user's status in the members list
        const memberItem = document.querySelector(`.member-item[data-id="${userId}"]`);
        if (memberItem) {
            const statusIndicator = memberItem.querySelector('.status-indicator');
            statusIndicator.classList.remove('online');
            statusIndicator.classList.add('offline');
            
            const statusText = memberItem.querySelector('.member-status');
            statusText.textContent = 'Offline';
        }
        
        // Show notification
        showNotification(`${username} left the room`);
    }
    
    function handleFileShared(data) {
        const { filename, fileId, sharedBy, fileSize, fileType } = data;
        
        // Add the file to the files list
        const filesList = document.querySelector('.files-list');
        if (filesList) {
            const fileHTML = `
                <div class="file-item" data-id="${fileId}">
                    <div class="file-icon" data-type="${fileType}"></div>
                    <div class="file-details">
                        <h4>${filename}</h4>
                        <p class="file-meta">
                            <span>${formatFileSize(fileSize)}</span> •
                            <span>Shared by ${sharedBy}</span> •
                            <span>Just now</span>
                        </p>
                    </div>
                    <div class="file-actions">
                        <button class="download-btn" data-id="${fileId}">Download</button>
                    </div>
                </div>
            `;
            filesList.insertAdjacentHTML('afterbegin', fileHTML);
            
            // Add event listener to the new download button
            const newDownloadBtn = filesList.querySelector(`.file-item[data-id="${fileId}"] .download-btn`);
            newDownloadBtn.addEventListener('click', function() {
                const fileId = this.getAttribute('data-id');
                const fileName = this.closest('.file-item').querySelector('h4').textContent;
                
                // Display download modal
                const downloadModal = document.getElementById('download-modal');
                const downloadFileName = document.getElementById('download-file-name');
                
                downloadFileName.textContent = fileName;
                downloadModal.style.display = 'block';
                
                // Start download
                downloadFile(fileId);
            });
        }
        
        // Show notification
        showNotification(`${sharedBy} shared a new file: ${filename}`);
    }
    
    function handleFileDownloaded(data) {
        const { filename, downloadedBy } = data;
        
        // Show notification
        showNotification(`${downloadedBy} downloaded ${filename}`);
    }
    
    function handleChatMessage(data) {
        const { message, sender } = data;
        
        // Implementation for chat functionality if added
    }
    
    // Helper functions
    function showNotification(message) {
        // Check if notifications container exists, create if not
        let notificationsContainer = document.getElementById('notifications-container');
        if (!notificationsContainer) {
            notificationsContainer = document.createElement('div');
            notificationsContainer.id = 'notifications-container';
            document.body.appendChild(notificationsContainer);
        }
        
        // Create notification element
        const notification = document.createElement('div');
        notification.className = 'notification';
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
    
    function formatFileSize(sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + ' B';
        } else if (sizeInBytes < 1048576) {
            return (sizeInBytes / 1024).toFixed(1) + ' KB';
        } else if (sizeInBytes < 1073741824) {
            return (sizeInBytes / 1048576).toFixed(1) + ' MB';
        } else {
            return (sizeInBytes / 1073741824).toFixed(1) + ' GB';
        }
    }
    
    // Initialize WebSocket connection
    connectWebSocket();
});