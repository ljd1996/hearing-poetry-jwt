package com.poetry.hearing.controller;

import com.poetry.hearing.domain.User;
import com.poetry.hearing.domain.UserCache;
import com.poetry.hearing.requestCached.HttpSessionRequestCache;
import com.poetry.hearing.requestCached.SavedRequest;
import com.poetry.hearing.service.OSSService;
import com.poetry.hearing.service.RedisService;
import com.poetry.hearing.service.UserService;
import com.poetry.hearing.util.Constant;
import com.poetry.hearing.util.Msg;
import com.poetry.hearing.util.TokenDetailImpl;
import com.poetry.hearing.util.TokenUtils;
import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

@Controller
public class PoetryController {

    @Autowired
    private UserService userService;

    @Autowired
    private OSSService ossService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private TokenUtils tokenUtils;

    @GetMapping("/")
    public String hello(){
        return "index";
    }

    @GetMapping("/error")
    public String error(){
        return "error";
    }

    private HttpSession getSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            session = request.getSession(true);
        }
        return session;
    }

    @GetMapping("/articleSum")
    public String articleSum(HttpServletRequest request, Map<String, Object> map,
                               @RequestParam("category") String category, @RequestParam("page") Integer page) {
        HttpSession session = getSession(request);
        List<Map<String, String>> objectInfoMaps = (List<Map<String, String>>) session.getAttribute(Constant.SESSION_KEYS_ALL + category);
        User user = (User) session.getAttribute(Constant.SESSION_USER);
        if (objectInfoMaps == null) {
            switch (category) {
                case Constant.MINE_ARTICLE:
                    if (user != null) {
                        objectInfoMaps = ossService.getObjectsOrdered("ancientPoetry/" + user.getEmail() + "/upload/");
                        objectInfoMaps.addAll(ossService.getObjectsOrdered("modernPoetry/" + user.getEmail() + "/upload/"));
                        objectInfoMaps.addAll(ossService.getObjectsOrdered("prose/" + user.getEmail() + "/upload/"));
                        objectInfoMaps.addAll(ossService.getObjectsOrdered("novel/" + user.getEmail() + "/upload/"));
                    } else {
                        return "registerOrLogin";
                    }
                    break;
                case Constant.COLLECT_ARTICLE:
                    if (user != null) {
                        objectInfoMaps = ossService.getObjectsOrdered("ancientPoetry/" + user.getEmail() + "/collect/");
                        objectInfoMaps.addAll(ossService.getObjectsOrdered("modernPoetry/" + user.getEmail() + "/collect/"));
                        objectInfoMaps.addAll(ossService.getObjectsOrdered("prose/" + user.getEmail() + "/collect/"));
                        objectInfoMaps.addAll(ossService.getObjectsOrdered("novel/" + user.getEmail() + "/collect/"));
                    } else {
                        return "registerOrLogin";
                    }
                    break;
                case Constant.MINE_PICTURE:
                    if (user != null) {
                        objectInfoMaps = ossService.getObjectsOrdered("picture/" + user.getEmail() + "/upload/");
                    } else {
                        return "registerOrLogin";
                    }
                    break;
                case Constant.COLLECT_PICTURE:
                    if (user != null) {
                        objectInfoMaps = ossService.getObjectsOrdered("picture/" + user.getEmail() + "/collect/");
                    } else {
                        return "registerOrLogin";
                    }
                    break;
                default:
                    objectInfoMaps = ossService.getObjectsOrdered(category + "/");
            }
            session.setAttribute(Constant.SESSION_KEYS_ALL + category, objectInfoMaps);
        }
        map.put("pageNow", page);
        int pageNum = objectInfoMaps.size()%Constant.numPerPage==0?
                objectInfoMaps.size()/Constant.numPerPage:objectInfoMaps.size()/Constant.numPerPage+1;
        map.put("pageNum", pageNum=pageNum==0?1:pageNum);
        map.put("recordNum", objectInfoMaps.size());
        List<Map<String, String>> showInfoMaps;
        if (page >= pageNum) {
            showInfoMaps=objectInfoMaps.subList((pageNum-1)*Constant.numPerPage, objectInfoMaps.size());
        }else {
            showInfoMaps=objectInfoMaps.subList((page-1)*Constant.numPerPage, page*Constant.numPerPage);
        }

        if (user != null) {
            if (category.equals("picture") || category.equals(Constant.MINE_PICTURE) || category.equals(Constant.COLLECT_PICTURE)) {
                List<Map<String, String>> pictureInfoMaps = (List<Map<String, String>>)
                        session.getAttribute(Constant.SESSION_KEYS_ALL + Constant.COLLECT_PICTURE);
                if (pictureInfoMaps == null) {
                    pictureInfoMaps = ossService.getObjectsOrdered("picture/" + user.getEmail() + "/collect/");
                    session.setAttribute(Constant.SESSION_KEYS_ALL + Constant.COLLECT_PICTURE, pictureInfoMaps);
                }
                for (Map<String, String> showInfoMap : showInfoMaps) {
                    for (Map<String, String> pictureInfoMap : pictureInfoMaps) {
                        if (pictureInfoMap.get("key").endsWith(showInfoMap.get("key").substring(showInfoMap.get("key").lastIndexOf("/")+1))) {
                            showInfoMap.put("hasCollect", "true");
                            break;
                        }
                    }
                }
            }
        }
        map.put("contentInfo", showInfoMaps);
        map.put("category", category);
        return "articleSum";
    }

    @GetMapping("/myself")
    public String myself(HttpServletRequest request){
        HttpSession session = getSession(request);
        User user = (User) session.getAttribute(Constant.SESSION_USER);
        if (session.getAttribute(Constant.SESSION_USER_HEAD) == null) {
            List<Map<String, String>> head = ossService.getObjectsOrdered("head/" + user.getEmail() + "/");
            if (head.size() > 0) {
                session.setAttribute(Constant.SESSION_USER_HEAD,
                        ossService.getUrlFromKey(head.get(0).get("key"),new Date(new Date().getTime() + 3600 * 1000)));
            }
        }
        return "myself";
    }

    @GetMapping("/connect")
    public String connect(){
        return "connect";
    }

    @PostMapping("/article")
    public String article(HttpServletRequest request, HttpServletResponse response,
                          Map<String, Object> map, @RequestParam("key") String key,
                          @RequestParam(value = "bgKey", required = false) String bgKey) throws IOException {
        HttpSession session = getSession(request);
        map.put("hasCollect", false);
        User user = (User) session.getAttribute(Constant.SESSION_USER);
        if (user != null) {
            List<Map<String, String>> objectInfoMaps = (List<Map<String, String>>)
                    session.getAttribute(Constant.SESSION_KEYS_ALL + Constant.COLLECT_ARTICLE);
            if (objectInfoMaps == null) {
                objectInfoMaps = ossService.getObjectsOrdered("ancientPoetry/" + user.getEmail() + "/collect/");
                objectInfoMaps.addAll(ossService.getObjectsOrdered("modernPoetry/" + user.getEmail() + "/collect/"));
                objectInfoMaps.addAll(ossService.getObjectsOrdered("prose/" + user.getEmail() + "/collect/"));
                objectInfoMaps.addAll(ossService.getObjectsOrdered("novel/" + user.getEmail() + "/collect/"));
                session.setAttribute(Constant.SESSION_KEYS_ALL + Constant.COLLECT_ARTICLE, objectInfoMaps);
            }
            for (Map<String, String> objectInfoMap:objectInfoMaps) {
                if (objectInfoMap.get("key").endsWith(key.substring(key.lastIndexOf("/")+1))) {
                    map.put("hasCollect", true);
                    break;
                }
            }
        }
        map.put("article", ossService.readFromOSS(key));
        map.put("key", key);
        map.put("bgKey", bgKey);
        return "article";
    }

    @PostMapping("/login")
    public void login(HttpServletRequest request, HttpServletResponse response,
                      String name, String passwd, boolean loginkeeping) throws IOException {
        Msg msg = userService.login(name, passwd, loginkeeping);
        if ((boolean)msg.getExtend().get(Constant.LOGIN_REGISTER_STATUS)) {
            String token = tokenUtils.generateToken(new TokenDetailImpl(name, passwd));
            System.out.println("token:" + token);

            User user = (User) msg.getExtend().get(Constant.MSG_LOGINING_USER);

            HttpSession session = getSession(request);
            session.setAttribute(Constant.SESSION_USER, user);

            HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
            SavedRequest saved = requestCache.getRequest(request, response);
            requestCache.removeRequest(request, response);
            if (saved != null) {
                String urlCached = saved.getRedirectUrl();
//                String url = urlCached.substring(urlCached.indexOf("/", urlCached.indexOf("//")+2));
                String key = saved.getParameterValues("key")[0];
                String category = key.substring(0, key.indexOf("/"));
//                submitPost2(urlCached, key, category);
                response.sendRedirect("/hearing/articleSum?category=" + category + "&page=1");
//                response.sendRedirect(url);
            } else {
                response.sendRedirect("/hearing/myself");
            }
        } else {
            response.sendRedirect("/hearing/login");
        }
    }

//    private void submitPost2(String url, String key, String category) throws IOException {
//        HttpClient client = HttpClients.createDefault();
//        HttpPost post = new HttpPost(url);
//
//        List <NameValuePair> params = new ArrayList<>();
//        params.add(new BasicNameValuePair("category", category));
//        params.add(new BasicNameValuePair("key", key));
//
//        try {
//            post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
//            HttpResponse httpResponse = client.execute(post);
//
//            HttpEntity entity = httpResponse.getEntity();
//            System.out.println("status:" + httpResponse.getStatusLine());
//            System.out.println("response content:" + EntityUtils.toString(entity));
//        } catch (UnsupportedEncodingException e1) {
//            e1.printStackTrace();
//        }
//    }

    @GetMapping("/registerOrLogin")
    public String registerOrLogin() {
        return "registerOrLogin";
    }

    @PostMapping("/register")
    public String register(HttpServletRequest request, @Valid User user, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return "redirect:registerOrLogin";
        }
        Msg msg = userService.register(user);
        if ((boolean)msg.getExtend().get(Constant.LOGIN_REGISTER_STATUS)){
            HttpSession session = getSession(request);
            session.setAttribute(Constant.SESSION_USER, user);
            return "redirect:myself";
        }
        return "registerOrLogin";
    }

    @PostMapping("/upload")
    public String uploadArticle(HttpServletRequest request, @RequestParam("file")MultipartFile file,
                                @RequestParam(value = "fileBg", required = false)MultipartFile fileBg,
                                @RequestParam("category") String category) throws Throwable {
        String source = ossService.copyFile(file, OSSService.localPath);
        String sourceBg = null;
        if (fileBg != null) {
            sourceBg = ossService.copyFile(fileBg, OSSService.localPath);
        }
        HttpSession session = getSession(request);
        User user = (User) session.getAttribute(Constant.SESSION_USER);
        if (user == null) {
            return "registerOrLogin";
        }
        ossService.upload(source, category + "/" + user.getEmail() + "/upload/" +
                source.substring(source.lastIndexOf("/") + 1), user.getEmail());
        if (fileBg != null) {
            ossService.upload(sourceBg, "articleBg/Bg" + source.substring(source.lastIndexOf("/") + 1,
                    source.lastIndexOf(".")) + sourceBg.substring(sourceBg.lastIndexOf(".")), user.getEmail());
        }
        session.removeAttribute(Constant.SESSION_KEYS_ALL + category);
        session.removeAttribute(Constant.SESSION_KEYS_ALL + Constant.MINE_PICTURE);
        session.removeAttribute(Constant.SESSION_KEYS_ALL + Constant.MINE_ARTICLE);
        return "myself";
    }

    @PostMapping("/uploadHead")
    public String uploadHead(HttpServletRequest request, @RequestParam("file")MultipartFile file) throws Throwable {
        String source = ossService.copyFile(file, OSSService.localPath);
        HttpSession session = getSession(request);
        User user = (User) session.getAttribute(Constant.SESSION_USER);
        if (user == null) {
            return "registerOrLogin";
        }
        List<Map<String, String>> head = ossService.getObjectsOrdered("head/" + user.getEmail() + "/");
        if (head.size() > 0) {
            ossService.delObjByKey(head.get(0).get("key"));
        }
        ossService.upload(source, "head/" + user.getEmail() + "/" + source.substring(source.lastIndexOf("/") + 1), user.getEmail());
        session.removeAttribute(Constant.SESSION_USER_HEAD);
        return "redirect:myself";
    }

    @PostMapping("/updateUserInfo")
    @ResponseBody
    public String updateUserInfo(HttpServletRequest request, @RequestParam("name") String name, @RequestParam("autograph") String autograph) {
        HttpSession session = getSession(request);
        User user = (User) session.getAttribute(Constant.SESSION_USER);
        if (user == null) {
            return "registerOrLogin";
        }
        Msg msg = userService.updateUserInfo(user.getEmail(), name, autograph);
        user = (User)msg.getExtend().get(Constant.MSG_LOGINING_USER);
        session.setAttribute(Constant.SESSION_USER, user);
        if (redisService.getCacheByKey(Constant.KEY_HAS_USER_CACHE) != null) {
            redisService.setUserCache(user);
        }
        return msg.getMsg();
    }

    @PostMapping("/collect")
    @ResponseBody
    public Msg collect(HttpServletRequest request,HttpServletResponse response,
                       @RequestParam("category") String category, @RequestParam("key") String key) {
        HttpSession session = getSession(request);
        User user = (User) session.getAttribute(Constant.SESSION_USER);
        if (user == null) {
            HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
            requestCache.saveRequest(request, response);
            return Msg.fail().add("msg", "请在登录后再进行操作!").add("collected", false);
        }
        String dest = category + "/" + user.getEmail() + "/collect/" + key.substring(key.lastIndexOf("/") + 1);
        ossService.copyObj(key, dest);
        session.removeAttribute(Constant.SESSION_KEYS_ALL + Constant.COLLECT_ARTICLE);
        session.removeAttribute(Constant.SESSION_KEYS_ALL + Constant.COLLECT_PICTURE);
        return Msg.success().add("msg", "收藏成功!").add("collected", true);
    }
}
