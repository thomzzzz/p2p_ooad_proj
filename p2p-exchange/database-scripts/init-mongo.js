/**
 * MongoDB initialization script for P2P Information Exchange System
 * This script creates the necessary collections and indexes for the application
 */

// Switch to the p2pexchange database (creates it if it doesn't exist)
db = db.getSiblingDB('p2pexchange');

// Create collections with schema validation
db.createCollection('users', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['username', 'passwordHash', 'email', 'role', 'createdAt', 'active'],
            properties: {
                username: {
                    bsonType: 'string',
                    description: 'Username must be a string and is required'
                },
                passwordHash: {
                    bsonType: 'string',
                    description: 'Password hash must be a string and is required'
                },
                email: {
                    bsonType: 'string',
                    description: 'Email must be a string and is required'
                },
                role: {
                    bsonType: 'string',
                    description: 'Role must be a string and is required'
                },
                createdAt: {
                    bsonType: 'date',
                    description: 'Created at timestamp is required'
                },
                lastLogin: {
                    bsonType: ['date', 'null'],
                    description: 'Last login timestamp'
                },
                active: {
                    bsonType: 'bool',
                    description: 'Active status is required'
                },
                profileAttributes: {
                    bsonType: 'object',
                    description: 'Profile attributes object'
                }
            }
        }
    }
});

db.createCollection('files', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['filename', 'originalFilename', 'contentType', 'size', 'path', 'ownerId', 'uploadDate'],
            properties: {
                filename: {
                    bsonType: 'string',
                    description: 'Stored filename must be a string and is required'
                },
                originalFilename: {
                    bsonType: 'string',
                    description: 'Original filename must be a string and is required'
                },
                contentType: {
                    bsonType: 'string',
                    description: 'Content type must be a string and is required'
                },
                size: {
                    bsonType: 'long',
                    description: 'Size must be a number and is required'
                },
                path: {
                    bsonType: 'string',
                    description: 'Path must be a string and is required'
                },
                ownerId: {
                    bsonType: 'string',
                    description: 'Owner ID must be a string and is required'
                },
                uploadDate: {
                    bsonType: 'date',
                    description: 'Upload date must be a date and is required'
                },
                checksum: {
                    bsonType: 'string',
                    description: 'Checksum for file integrity'
                },
                encryptionType: {
                    bsonType: 'string',
                    description: 'Encryption algorithm used'
                },
                metadata: {
                    bsonType: 'object',
                    description: 'Additional metadata for the file'
                }
            }
        }
    }
});

db.createCollection('rooms', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['name', 'ownerId', 'members', 'accessLevel'],
            properties: {
                name: {
                    bsonType: 'string',
                    description: 'Name must be a string and is required'
                },
                ownerId: {
                    bsonType: 'string',
                    description: 'Owner ID must be a string and is required'
                },
                members: {
                    bsonType: 'array',
                    description: 'Members array is required'
                },
                creators: {
                    bsonType: 'array',
                    description: 'Creators array'
                },
                sharedFiles: {
                    bsonType: 'array',
                    description: 'Shared files array'
                },
                accessLevel: {
                    enum: ['PUBLIC', 'RESTRICTED', 'PRIVATE'],
                    description: 'Access level must be one of the enum values'
                }
            }
        }
    }
});

db.createCollection('peers', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['userId', 'ipAddress', 'port', 'online', 'lastSeen'],
            properties: {
                userId: {
                    bsonType: 'string',
                    description: 'User ID must be a string and is required'
                },
                ipAddress: {
                    bsonType: 'string',
                    description: 'IP address must be a string and is required'
                },
                port: {
                    bsonType: 'int',
                    description: 'Port must be an integer and is required'
                },
                online: {
                    bsonType: 'bool',
                    description: 'Online status must be a boolean and is required'
                },
                lastSeen: {
                    bsonType: 'date',
                    description: 'Last seen timestamp must be a date and is required'
                }
            }
        }
    }
});

db.createCollection('system_logs', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['timestamp', 'level', 'message'],
            properties: {
                timestamp: {
                    bsonType: 'date',
                    description: 'Timestamp must be a date and is required'
                },
                level: {
                    enum: ['DEBUG', 'INFO', 'WARN', 'ERROR'],
                    description: 'Log level must be one of the enum values'
                },
                message: {
                    bsonType: 'string',
                    description: 'Message must be a string and is required'
                },
                userId: {
                    bsonType: 'string',
                    description: 'User ID associated with the log'
                },
                details: {
                    bsonType: 'object',
                    description: 'Additional details for the log entry'
                }
            }
        }
    }
});

// Create indexes for better query performance
db.users.createIndex({ username: 1 }, { unique: true });
db.users.createIndex({ email: 1 }, { unique: true });
db.users.createIndex({ role: 1 });
db.users.createIndex({ active: 1 });

db.files.createIndex({ ownerId: 1 });
db.files.createIndex({ filename: 1 });
db.files.createIndex({ contentType: 1 });
db.files.createIndex({ uploadDate: 1 });

db.rooms.createIndex({ ownerId: 1 });
db.rooms.createIndex({ members: 1 });
db.rooms.createIndex({ accessLevel: 1 });
db.rooms.createIndex({ sharedFiles: 1 });

db.peers.createIndex({ userId: 1 }, { unique: true });
db.peers.createIndex({ ipAddress: 1 });
db.peers.createIndex({ online: 1 });
db.peers.createIndex({ lastSeen: 1 });

db.system_logs.createIndex({ timestamp: 1 });
db.system_logs.createIndex({ level: 1 });
db.system_logs.createIndex({ userId: 1 });

// Create admin user if it doesn't exist
if (db.users.countDocuments({ username: 'admin' }) === 0) {
    db.users.insertOne({
        username: 'admin',
        // Default password: admin123 (should be changed immediately)
        // This is a bcrypt hash of 'admin123'
        passwordHash: '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS',
        email: 'admin@example.com',
        role: 'ROLE_ADMIN',
        createdAt: new Date(),
        active: true,
        profileAttributes: {}
    });
    
    print('Created default admin user');
}

// Create system settings if they don't exist
if (db.system_settings.countDocuments() === 0) {
    db.system_settings.insertOne({
        fileManagement: {
            maxFileSize: 100,  // MB
            allowedFileTypes: [
                'image/*',
                'text/*',
                'application/pdf',
                'application/msword',
                'application/vnd.openxmlformats-officedocument.*',
                'application/zip',
                'application/x-rar-compressed'
            ],
            storagePath: './uploads'
        },
        security: {
            tokenExpiration: 24,  // hours
            passwordPolicy: 'strong',
            enforce2FA: false
        },
        roomManagement: {
            maxRoomMembers: 50,
            maxRoomFiles: 100,
            roomLinkExpiry: 24  // hours
        },
        system: {
            logLevel: 'INFO',
            sessionTimeout: 30,  // minutes
            maintenanceMode: false
        },
        updatedAt: new Date()
    });
    
    print('Created default system settings');
}

print('MongoDB initialization complete');