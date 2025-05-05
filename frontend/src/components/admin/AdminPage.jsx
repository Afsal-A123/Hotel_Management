
import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import ApiService from '../../service/ApiService';

const AdminPage = () => {
    const [adminName, setAdminName] = useState('');
    const [isLoading, setIsLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchAdminName = async () => {
            try {
                setIsLoading(true);
                const response = await ApiService.getUserProfile();
                setAdminName(response.user.name);
            } catch (error) {
                console.error('Error fetching admin details:', error.message);
            } finally {
                setIsLoading(false);
            }
        };

        fetchAdminName();
    }, []);

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('role');
        navigate('/login');
    };

    return (
        <div>
            <div className="container">
                <div className="admin-page-container bg-white p-6 rounded-lg shadow-md">
                    {isLoading ? (
                        <div className="admin-loading">Loading admin details...</div>
                    ) : (
                        <>
                            <h1 className="welcome-message">Welcome, {adminName}</h1>
                            <div className="admin-actions">
                                <button className="admin-button" onClick={() => navigate('/admin/manage-rooms')}>
                                    Manage Rooms
                                </button>
                                <button className="admin-button" onClick={() => navigate('/admin/manage-bookings')}>
                                    Manage Bookings
                                </button>
                                <button 
                                    className="admin-button logout-button bg-red-500 hover:bg-red-600 text-white"
                                    onClick={handleLogout}
                                >
                                    Logout
                                </button>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
}

export default AdminPage;
