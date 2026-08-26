import './AdminLayout.css';
import Header from "../common/Header.jsx";
import {Button2} from "../common/Button.jsx";
import {useNavigate} from "react-router-dom";
import {useAuth} from "../../context/AuthContext.jsx";

// 관리자 페이지 공통 레이아웃 + Route Guard.
// Backend의 /api/admin/** 보호가 최종 보안이며, 여기서는 ADMIN이 아닌 사용자에게
// 관리자 화면 자체를 렌더링하지 않는 선에서만 처리한다.
const AdminLayout = ({active, children}) => {
  const {isAdmin, isLoading} = useAuth();
  const nav = useNavigate();

  if (isLoading) {
    return (
        <>
          <Header/>
        </>
    );
  }

  if (!isAdmin) {
    return (
        <>
          <Header/>
          <div className="admin-blocked">
            <p>관리자만 접근할 수 있는 페이지입니다.</p>
            <Button2 buttonText={"돌아가기"} buttonEvent={() => nav("/")}/>
          </div>
        </>
    );
  }

  return (
      <>
        <Header/>
        <div className="admin-container">
          <div className="admin-nav">
            <div className="admin-nav-title">관리자</div>
            <div
                className={`admin-nav-item${active === 'dashboard' ? ' active' : ''}`}
                onClick={() => nav("/admin")}
            >
              Dashboard
            </div>
            <div
                className={`admin-nav-item${active === 'users' ? ' active' : ''}`}
                onClick={() => nav("/admin/users")}
            >
              Users
            </div>
            <div
                className={`admin-nav-item${active === 'posts' ? ' active' : ''}`}
                onClick={() => nav("/admin/posts")}
            >
              Posts
            </div>
            <div
                className={`admin-nav-item${active === 'comments' ? ' active' : ''}`}
                onClick={() => nav("/admin/comments")}
            >
              Comments
            </div>
            <div
                className={`admin-nav-item${active === 'monitoring' ? ' active' : ''}`}
                onClick={() => nav("/admin/monitoring")}
            >
              Monitoring
            </div>
            <div
                className={`admin-nav-item${active === 'operations' ? ' active' : ''}`}
                onClick={() => nav("/admin/operations")}
            >
              Operations
            </div>
            <hr className="admin-nav-divider"/>
            <div className="admin-nav-item" onClick={() => nav("/")}>
              SolveLog로 돌아가기
            </div>
          </div>
          <div className="admin-content">
            {children}
          </div>
        </div>
      </>
  );
};

export default AdminLayout;
