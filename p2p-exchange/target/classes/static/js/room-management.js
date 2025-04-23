document.addEventListener('DOMContentLoaded', function() {
    // Room creation
    const createRoomBtn = document.getElementById('create-room-btn');
    
    if (createRoomBtn) {
        createRoomBtn.addEventListener('click', function() {
            const roomName = prompt('Enter a name for the new room:');
            if (roomName) {
                createRoom(roomName);
            }
        });
    }
    
    // Room entry
    const enterRoomBtns = document.querySelectorAll('.action-btn.enter');
    enterRoomBtns.forEach(button => {
        button.addEventListener('click', function() {
            const roomId = this.getAttribute('data-id');
            window.location.href = `/rooms/${roomId}`;
        });
    });
    
    // Share room link
    const shareRoomBtns = document.querySelectorAll('.action-btn.share-link, #share-room-btn');
    shareRoomBtns.forEach(button => {
        button.addEventListener('click', function() {
            let roomId;
            if (this.id === 'share-room-btn') {
                // We're in the room page, get roomId from URL
                const urlParts = window.location.pathname.split('/');
                roomId = urlParts[urlParts.length - 1];
            } else {
                roomId = this.getAttribute('data-id');
            }
            
            generateRoomLink(roomId);
        });
    });
    
    // Room settings
    const roomSettingsBtns = document.querySelectorAll('.action-btn.settings');
    roomSettingsBtns.forEach(button => {
        button.addEventListener('click', function() {
            const roomId = this.getAttribute('data-id');
            showRoomSettings(roomId);
        });
    });
    
    // Helper functions
    function createRoom(roomName) {
        fetch('/api/rooms', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ name: roomName })
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error('Failed to create room');
        })
        .then(data => {
            alert(`Room "${roomName}" created successfully!`);
            window.location.reload(); // Refresh to show new room
        })
        .catch(error => {
            console.error('Error creating room:', error);
            alert('Error creating room. Please try again.');
        });
    }
    
    function generateRoomLink(roomId) {
        fetch(`/api/rooms/${roomId}/link`, {
            method: 'POST'
        })
        .then(response => response.json())
        .then(data => {
            const shareLink = `${window.location.origin}/rooms/join/${data.linkToken}`;
            
            // Create a temporary input to copy to clipboard
            const tempInput = document.createElement('input');
            tempInput.value = shareLink;
            document.body.appendChild(tempInput);
            tempInput.select();
            document.execCommand('copy');
            document.body.removeChild(tempInput);
            
            alert('Room link copied to clipboard! Share it with others to invite them.');
        })
        .catch(error => {
            console.error('Error generating room link:', error);
            alert('Error generating room link. Please try again.');
        });
    }
    
    function showRoomSettings(roomId) {
        // Fetch room data first
        fetch(`/api/rooms/${roomId}`)
            .then(response => response.json())
            .then(room => {
                // Create a modal dialog for room settings
                const modalHTML = `
                    <div id="room-settings-modal" class="modal">
                        <div class="modal-content">
                            <span class="close">&times;</span>
                            <h3>Room Settings: ${room.name}</h3>
                            <form id="room-settings-form">
                                <div class="form-group">
                                    <label for="room-name">Room Name</label>
                                    <input type="text" id="room-name" name="name" value="${room.name}" required>
                                </div>
                                <div class="form-group">
                                    <label for="access-level">Access Level</label>
                                    <select id="access-level" name="accessLevel">
                                        <option value="PUBLIC" ${room.accessLevel === 'PUBLIC' ? 'selected' : ''}>Public (Anyone with link can join)</option>
                                        <option value="RESTRICTED" ${room.accessLevel === 'RESTRICTED' ? 'selected' : ''}>Restricted (Require approval)</option>
                                        <option value="PRIVATE" ${room.accessLevel === 'PRIVATE' ? 'selected' : ''}>Private (Invite only)</option>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <h4>Members</h4>
                                    <div class="members-settings-list">
                                        ${generateMembersList(room.members)}
                                    </div>
                                </div>
                                <div class="form-actions">
                                    <button type="submit" class="button primary">Save Changes</button>
                                    <button type="button" class="button danger" id="delete-room-btn">Delete Room</button>
                                </div>
                            </form>
                        </div>
                    </div>
                `;
                
                // Add the modal to the document
                const modalContainer = document.createElement('div');
                modalContainer.innerHTML = modalHTML;
                document.body.appendChild(modalContainer);
                
                const modal = document.getElementById('room-settings-modal');
                const closeBtn = modal.querySelector('.close');
                const form = document.getElementById('room-settings-form');
                const deleteBtn = document.getElementById('delete-room-btn');
                
                modal.style.display = 'block';
                
                // Close button functionality
                closeBtn.addEventListener('click', function() {
                    modal.style.display = 'none';
                    setTimeout(() => {
                        document.body.removeChild(modalContainer);
                    }, 300);
                });
                
                // Form submission
                form.addEventListener('submit', function(e) {
                    e.preventDefault();
                    
                    const formData = {
                        name: document.getElementById('room-name').value,
                        accessLevel: document.getElementById('access-level').value
                    };
                    
                    updateRoomSettings(roomId, formData, modal, modalContainer);
                });
                
                // Delete room
                deleteBtn.addEventListener('click', function() {
                    if (confirm('Are you sure you want to delete this room? This action cannot be undone.')) {
                        deleteRoom(roomId, modal, modalContainer);
                    }
                });
                
                // Member removal
                const removeButtons = modal.querySelectorAll('.remove-member-btn');
                removeButtons.forEach(button => {
                    button.addEventListener('click', function() {
                        const memberId = this.getAttribute('data-id');
                        if (confirm('Remove this member from the room?')) {
                            removeMember(roomId, memberId, this.closest('.member-item'));
                        }
                    });
                });
            })
            .catch(error => {
                console.error('Error fetching room data:', error);
                alert('Error loading room settings. Please try again.');
            });
    }
    
    function generateMembersList(members) {
        return members.map(member => `
            <div class="member-item">
                <span>${member.username}</span>
                <button type="button" class="remove-member-btn" data-id="${member.id}">&times;</button>
            </div>
        `).join('');
    }
    
    function updateRoomSettings(roomId, formData, modal, modalContainer) {
        fetch(`/api/rooms/${roomId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        })
        .then(response => {
            if (response.ok) {
                alert('Room settings updated successfully!');
                modal.style.display = 'none';
                setTimeout(() => {
                    document.body.removeChild(modalContainer);
                    window.location.reload(); // Refresh to show updated data
                }, 300);
            } else {
                throw new Error('Failed to update room settings');
            }
        })
        .catch(error => {
            console.error('Error updating room settings:', error);
            alert('Error updating room settings. Please try again.');
        });
    }
    
    function deleteRoom(roomId, modal, modalContainer) {
        fetch(`/api/rooms/${roomId}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (response.ok) {
                alert('Room deleted successfully!');
                modal.style.display = 'none';
                setTimeout(() => {
                    document.body.removeChild(modalContainer);
                    window.location.reload(); // Refresh to show updated data
                }, 300);
            } else {
                throw new Error('Failed to delete room');
            }
        })
        .catch(error => {
            console.error('Error deleting room:', error);
            alert('Error deleting room. Please try again.');
        });
    }
    
    function removeMember(roomId, memberId, memberElement) {
        fetch(`/api/rooms/${roomId}/members/${memberId}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (response.ok) {
                memberElement.remove();
                alert('Member removed from room.');
            } else {
                throw new Error('Failed to remove member');
            }
        })
        .catch(error => {
            console.error('Error removing member:', error);
            alert('Error removing member. Please try again.');
        });
    }
});