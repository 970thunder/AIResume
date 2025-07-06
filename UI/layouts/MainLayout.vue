.sidebar {
background-color: #ffffff;
border-right: 1px solid #e9e9e9;
transition: width 0.3s ease;
display: flex;
flex-direction: column;
overflow-y: auto; /* Allow vertical scrolling */
overflow-x: hidden; /* Prevent horizontal scrolling */
scrollbar-width: none; /* Hide scrollbar for Firefox */
-ms-overflow-style: none; /* Hide scrollbar for IE and Edge */
}

.sidebar::-webkit-scrollbar {
display: none; /* Hide scrollbar for Chrome, Safari, and Opera */
}

.sidebar-header {
white-space: nowrap;
overflow: hidden;
}

.sidebar-menu {
flex-grow: 1;
border-right: none;
background: #ffffff;
/* Remove overflow handling from here */
}

.el-menu-item {
font-weight: 500;
}