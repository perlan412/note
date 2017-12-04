public List<SimBean> getSimListBelow22(Context mContext) {
		List<SimBean> list = new ArrayList<SimBean>();
		try {
			Class<?> SimManager = Class.forName("android.sim.SimManager");
			
			Method get = SimManager.getMethod("get" , Context.class);
			
			Object obj = (Object) get.invoke(SimManager , mContext);
			
			Method activeSims = SimManager.getMethod("getActiveSims");
			
			Object[] objs = (Object[]) activeSims.invoke(obj);
			
			Class<?> Sim = Class.forName("android.sim.Sim");
			
			Method getColorIndex = Sim.getMethod("getColorIndex");
			Method getName = Sim.getMethod("getName");
			Method getPhoneId = Sim.getMethod("getPhoneId");
				
			int lastSlot = -1;
			for (Object siminfo : objs) {
				int slot = (Integer) getColorIndex.invoke(siminfo);
				String name = (String) getName.invoke(siminfo);
				int phoneId = (Integer) getPhoneId.invoke(siminfo);
				if(isIccCardActivatedBelow22(mContext, slot)){
					SimBean bean = new SimBean();
					bean.setSlot(slot);
					bean.setSimId(phoneId);
					bean.setDisName(name);
					if (!isIccCardActivatedBelow22(mContext, slot)) {
						continue ;
					}
					if(bean.getSlot()>lastSlot){
						list.add(bean);
						lastSlot = bean.getSlot();
					}else{
						list.add(0, bean);
					}
				}
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list ;
	}
