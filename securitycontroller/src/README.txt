订阅部分的说明：

sa(security app)向sc(security controller)发送订阅请求，格式如下
req.json
{	
	"module":"IFlowMonitor",
	"event_type":"RETRIEVED_FLOW"
	"subscription":
	{
		"subscribed_value": ["object","record"]
		'op' : 'AND',
		'ctype' : 'compcondition'
		"subscription":
		{
			"subscribed_value": "object",
			"subscribed_condition": 
			{
				"subscribed_condition":
				{
					'key': 'object.byte_count',
					'op' : 'GT',
					'value': 5000,
					'vtype' : 'int',					
					'ctype' : 'opcondition'
				}
				"subscribed_condition":
				{
				}
				...
				"subscribed_condition":
				{
					'key': 'object.protocol',
					'op' : 'EQ',
					'value': 'TCP',
					'vtype' : 'String',
					'ctype' : 'opcondition'
				}
				'op' : 'AND',
				'ctype' : 'compcondition'
			}
		}
		
		"subscription":{
			"subscribed_value": "record",
			"subscribed_condition": 
			{
				'key': 'db.flows.time',
				'op' : 'GT',
				'value': 'NOW - 5min',
				'vtype' : 'datetime',
				'ctype' : 'opcondition'				
			}	
			"subscribed_condition": 
			{
				'key': 'dbobject.byte_count',
				'op' : 'GT',
				'value': 'object.byte_count+1000',
				'vtype' : 'datetime',
				'ctype' : 'opcondition'				
			}
			'op' : 'AND',
			'ctype' : 'compcondition'				
		}
	}
}
json格式必须是一个compcondition类型的subscription开始的，并递归地包含若干层subscription节点，叶节点的类型为opcondition，中间节点的类型为compcondition。

compcondition节点包含多个子subscription节点。subscribed_value为返回类型，record表示数据库记录，那么子节点都是数据库的查询操作；object表示事件的主体，子节点都是该主体的属性与某些变量的比较操作。子subscription节点的操作结果可用op符号进行与或操作，可加非操作，返回record或object的列表
opcondition是一个具体的操作，key是主体属性或数据库字段，op是布尔比较符，value是需要比较的变量或常量，vtype是比较双方的数值类型。

最终sc解析这个json请求，并生成一个EventSubscription实例es，es为根节点，compcondition对应的是CompoundEventSubscription，opcondition对应的是OperatorEventSubscription。