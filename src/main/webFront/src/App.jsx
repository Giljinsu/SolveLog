import './App.css'
import {Routes, Route} from "react-router-dom";
import {createContext, useState} from "react";
import Home from "./page/Home.jsx";
import Post from "./page/Post.jsx";
import {AuthProvider} from "./context/AuthContext.jsx"
import Signup from "./page/Signup.jsx";
import axios from "axios";
import {SearchProvider} from "./context/SearchContext.jsx";
import PostEdit from "./page/PostEdit.jsx";
import PostNew from "./page/PostEdit.jsx";
import 'highlight.js/styles/atom-one-dark.css';
import TempPost from "./page/TempPost.jsx";
import {PopupProvider} from "./context/PopupContext.jsx";
import MyPage from "./page/MyPage.jsx";
import LoadingPopup from "./components/loading/LoadingPopup.jsx";
import {useLoadingStore} from "./hooks/useLoadingStore.js";
import ResetPassword from "./page/ResetPassword.jsx";
import AdminDashboard from "./page/admin/AdminDashboard.jsx";
import AdminUsers from "./page/admin/AdminUsers.jsx";
import AdminPosts from "./page/admin/AdminPosts.jsx";
import AdminComments from "./page/admin/AdminComments.jsx";
import AdminMonitoring from "./page/admin/AdminMonitoring.jsx";
import AdminMonitoringDetail from "./page/admin/AdminMonitoringDetail.jsx";
import AdminOperations from "./page/admin/AdminOperations.jsx";

// export const PostStateContext = createContext();
// export const PostSDispatchContext = createContext();
  export const LoginContext = createContext();
  export const LoginDispatchContext = createContext();

  const getPostList = async () => {
    try {
      let api = await axios.get("/api/getPostList", searchCondition);
      setPostList(api.data.content);

    } catch (e) {
      console.log(e);
    }
  }

function App() {
  const [isLoginOpen, setIsLoginOpen] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const {loading} = useLoadingStore();

  return (
      <>
        {loading && <LoadingPopup />}
        <SearchProvider>
          <AuthProvider>
            <PopupProvider>
                <LoginContext.Provider value={{isLoginOpen, isSearchOpen}}>
                  <LoginDispatchContext.Provider value={{
                    setIsLoginOpen,
                    setIsSearchOpen,
                  }}>
                    <Routes>
                      <Route path={"/"} element={<Home /> }></Route>
                      <Route path={"/post/:postId/:title"} element={<Post /> }></Route>
                      <Route path={"/signup"} element={<Signup />}></Route>
                      <Route path={"/postEdit"} element={<PostEdit />}></Route>
                      <Route path={"/tempPost"} element={<TempPost />}></Route>
                      <Route path={"/myPage/:username"} element={<MyPage />}></Route>
                      <Route path={"/resetPw?"} element={<ResetPassword />}></Route>
                      <Route path={"/admin"} element={<AdminDashboard />}></Route>
                      <Route path={"/admin/users"} element={<AdminUsers />}></Route>
                      <Route path={"/admin/posts"} element={<AdminPosts />}></Route>
                      <Route path={"/admin/comments"} element={<AdminComments />}></Route>
                      <Route path={"/admin/monitoring"} element={<AdminMonitoring />}></Route>
                      <Route path={"/admin/monitoring/:issueId"} element={<AdminMonitoringDetail />}></Route>
                      <Route path={"/admin/operations"} element={<AdminOperations />}></Route>
                      {/*<Route path={"/postNew"} element={<PostNew />}></Route>*/}
                    </Routes>
                  </LoginDispatchContext.Provider>
                </LoginContext.Provider>
            </PopupProvider>
          </AuthProvider>
        </SearchProvider>
      </>
  )
}

export default App
