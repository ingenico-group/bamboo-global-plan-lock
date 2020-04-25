[@ui.bambooSection title="Global Plan Lock"]

	[@ww.checkbox 
		label='Enable Global Lock for this plan?'
        name='custom.com.ingenico.bamboo.bpgl.globalLock_enabled'
        toggle='true' /]

    [@ww.textfield 
    	label='Global Lock Name'
        name='custom.com.ingenico.bamboo.bpgl.globalLock_key' 
        description='All plans with the EXACT SAME KEY will be limited to one single concurrent build across all agents'/]
                   
[/@ui.bambooSection]