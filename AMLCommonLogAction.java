package com.gtone.aml.common.action;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.gtone.aml.basic.common.data.DataObj;
import com.gtone.aml.dao.common.MDaoUtil;
import com.gtone.aml.server.common.commonUtil;
import com.gtone.aml.user.SessionAML;
import com.gtone.express.server.actions.ExpressAction;
import com.itplus.common.server.user.SessionHelper;

import jspeed.base.util.StringHelper;

public class AMLCommonLogAction extends ExpressAction {

	String amlLogYN = jspeed.base.property.PropertyService.getInstance().getProperty("aml.config","AMLlogSaveOption");
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public void amlLogInsert(SessionAML sessionAML, SessionHelper helper,DataObj input, HashMap output, String classNM, HashMap paramCopy) throws Exception {
    	
		 MDaoUtil mDao = new MDaoUtil();
		 HashMap params = new HashMap();
		 
		try {
			if(amlLogYN != null) {
				if(amlLogYN.equals("Y")) {
					
					String loginID = helper.getLoginId();
					String userNm  = helper.getUserName();
					String deptID  = helper.getDeptId().toString();
					String eventLog = StringHelper.nvl((String)input.getText("eventLog"),"");
					String methodID = StringHelper.nvl((String)input.getText("methodID"),"");
					String sqlID = StringHelper.evl((String) input.get("sqlID"), StringHelper.nvl((String)classNM,"") + (!"".equals(methodID) ? "_" + methodID : ""));
		    		String pageID_Nvl = classNM.substring(classNM.lastIndexOf("."));
		    		// 명명규칙에 맞지 않는 class인 경우 && 화면에서 호출한 경우 null 처리
		    		if(pageID_Nvl.indexOf("Action") > -1 || pageID_Nvl.indexOf("Controller") > -1 || "Y".equals(eventLog)){
		    			pageID_Nvl = null;
		    		}else{
		    			pageID_Nvl = pageID_Nvl.replace(".", "");
		    		}
		    	  
		    		String PSIF_INCL_YN = "N";
		    		StringBuffer param_Value= new StringBuffer();
		    		String paramVal = "";
		    		methodID = methodID.toUpperCase();
		    		String eventCD = "";
		    		String dldNcnt = (String) input.get("dldNcnt");
		    		String logCntn = (String) input.get("logCntn");
		    		
		    		/*방화벽이나 클라우드로 운영하는 경우 클라이언트의 원 IP주소를 가져올 수 없을 때 다음 함수 사용 : X-FORWARDED-FOR*/
		    		String USER_IP ="";
		    		USER_IP = StringHelper.nvl((String)sessionAML.req.getHeader("X-FORWARDED-FOR"), (String)sessionAML.getRequest().getRemoteAddr());
		    		
		    		// 파라미터
		            if (paramCopy != null) {
		                Set set = paramCopy.keySet();
		                Iterator iterator = set.iterator();
		                while (iterator.hasNext()) {
		                    String name = (String)iterator.next();
		                    if(paramCopy.get(name) instanceof java.lang.String) {
		                    	
		                    	param_Value.append("|"+name + ":" + StringHelper.evl(paramCopy.get(name), "")); 
		                    	
		                    }else {
		                    	param_Value.append(name + ":" + paramCopy.get(name)); 
		                    }	
		                }
		                paramVal = byteCuter(param_Value.toString(), 1800);
		            }
		            
		            //개인정보 포함 여부 : RNMCNO-실명번호
		            if(param_Value.toString().indexOf("RNMCNO") > -1) {
		            	PSIF_INCL_YN = "Y";
	    			}else {
	    				PSIF_INCL_YN = "N";
	    			}
		            
		            //이벤트구분 코드 : R-조회, C-등록, U-수정, D-삭제
		            if(methodID.indexOf("SEARCH") > -1 || methodID.indexOf("LIST") > -1 || methodID.indexOf("COUNT") > -1) {
		            	eventCD = "R";
	    			}else if(methodID.indexOf("INSERT") > -1 || methodID.indexOf("SAVE") > -1 ){
	    				eventCD = "C";
	    			}else if(methodID.indexOf("UPDATE") > -1){
	    				eventCD = "U";
	    			}else if(methodID.indexOf("DELETE") > -1 || methodID.indexOf("DEL") > -1 ){
	    				eventCD = "D";
	    			}else {
	    				eventCD = "R";
	    			}
		    		params.put("LOGIN_ID", StringHelper.nvl((String)loginID, ""));  //로그인ID
		    		params.put("USER_NM", StringHelper.nvl((String)userNm, ""));	 //사용자명
		    		params.put("USER_IP", USER_IP); //사용자IP
		    		params.put("MNU_ID", StringHelper.nvl(pageID_Nvl, input.getText("pageID"))); //메뉴ID
		    		params.put("PGE_ID", StringHelper.nvl(pageID_Nvl, input.getText("pageID"))); //페이지ID
		    		params.put("BRCH_NO", StringHelper.evl(deptID, "")); //지점코드
		    		params.put("PARAM_VAL", StringHelper.evl(paramVal,"")); //파라미터값
		    		params.put("SQL_ID_VAL", StringHelper.nvl(sqlID,"")); //SQLID값
		    		params.put("EVNT_DVCD", eventCD); //이벤트구분코드
		    		params.put("PSIF_INCL_YN", StringHelper.evl(PSIF_INCL_YN,"")); //개인정보포함여부
		    		params.put("DLD_NCNT", (dldNcnt==null||dldNcnt=="" ? "0" : dldNcnt)); //다운로드건수
		    		params.put("LOG_CNTN", logCntn);  //로그내용
		    		
		    		mDao.begin();
		    		
		    		mDao.setData("AML_00_LoginInfo_ACTION", params);
		    		 
				    mDao.commit();
				}
			}
    	}catch(Exception e){
    		e.printStackTrace();
            try { if (mDao       != null) mDao.rollback();       } catch (Exception ignore) {} 
    	}finally {
    		try { if (mDao       != null) mDao.close();       } catch (Exception ignore) {}      
    	}

    }
	
	/**
	 * 문자열 byte길이만큼 자르기
	 * @param st	  : 문자열
	 * @param cutLeng : 커트 할 길이
	 * @return
	 */
	public String byteCuter(String st, int cutLeng){
		if(st.toString().getBytes().length > cutLeng){
			StringBuilder sb = new StringBuilder(cutLeng);
			int nCnt = 0;
			
			for(char ch:st.toString().toCharArray()){
				nCnt += String.valueOf(ch).getBytes().length;
				if(nCnt > cutLeng) break;
				sb.append(ch);
			}
			
			return sb.toString() + "..";
		}else{
			return st;
		}
	}
	
	@SuppressWarnings({ "rawtypes" })
    public void amlLogInsert(HttpServletRequest request, Map inHash){
        try {
            // [1] 파라메터 셋팅
        	DataObj input = new DataObj();
        	// [1-2] 파라미터 복사
            if (inHash != null) {
                Set set = inHash.keySet();
                Iterator iterator = set.iterator();
                while (iterator.hasNext()) {
                    String name = (String)iterator.next();
                    if(inHash.get(name) instanceof java.lang.String)
                    	input.add(name, StringHelper.evl(inHash.get(name), ""));
                    else
                    	input.add(name, inHash.get(name));
                }
            }
            // [1-3] session 정보를 객체에 넣는다.
            SessionHelper helper = new  SessionHelper(request.getSession());
            // [1-4] AML 에서 사용되는 공통 Session 정보            
            SessionAML sessionAML = (SessionAML) Class.forName(commonUtil.getAMLSessionClassName()).getConstructor(new Class[] {HttpServletRequest.class}).newInstance(new Object[] {request});
            // [1-5] 로그인된 경우 처리
            if("1".equals(sessionAML.checkLogin())){
            	input.put("SessionHelper", helper);
                input.put("SessionAML", sessionAML);
                // [1-6] 호출 하는 class 정보
                String classNm = (String) inHash.get("classNm");
                if ( classNm == null || "".equals(classNm) ) {
                    GetResult getResult = new GetResult();       
                    classNm = getResult.getClassNM(input);
                }
            
                // [2]  AML Page Log 등록 처리 모듈
            	amlLogInsert(sessionAML, helper, input, null, classNm, input);
            }
        } catch(Exception e) {
            
        }
    }
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HashMap execute(HashMap params) throws Exception {
		
		MDaoUtil mDao = new MDaoUtil();
		HashMap output = new HashMap();
		try {
			if(amlLogYN != null) {
				if(amlLogYN.equals("Y")) {
					
					String eventLog = StringHelper.nvl((String)params.get("eventLog"),"");
					if("Y".equals(eventLog)){
						amlLogInsert(getRequest(), params);
					}else{
						mDao.begin();
						mDao.setData("AML_00_LoginInfo_ACTION", params);
						mDao.commit();
					}
					output.put("RESULT", "1");
				}
			}	
			
		}catch(Exception e){
    		e.printStackTrace();
            try { if (mDao       != null) mDao.rollback();       } catch (Exception ignore) {} 
    	}finally {
    		try { if (mDao       != null) mDao.close();       } catch (Exception ignore) {}      
    	}	
		return output;
	}
	
	
}
