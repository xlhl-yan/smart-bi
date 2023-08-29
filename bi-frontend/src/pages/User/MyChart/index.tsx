import { listChartByPageUsingPOST } from '@/services/smart-bi/chartController';
import { useModel } from '@umijs/max';
import { Avatar, Card, List, message } from 'antd';
import Search from 'antd/es/input/Search';
import ReactECharts from 'echarts-for-react';
import React, { useEffect, useState } from 'react';

/**
 * 我的图表页面
 * @constructor
 */
const MyChart: React.FC = () => {
  const initParams = {
    current: 1,
    pageSize: 4,
  };
  const [chartList, setChartList] = useState<API.Chart[]>([]);
  const [total, setTotal] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(false);
  const [searchParams, setSearchParams] = useState<API.ChartQueryRequest>({ ...initParams });
  const { initialState } = useModel('@@initialState');
  const { currentUser } = initialState;
  const loadData = async () => {
    setLoading(true);
    try {
      const res = await listChartByPageUsingPOST(searchParams);
      if (res.code === 0 || res.data) {
        setChartList(res.data?.records ?? []);
        setTotal(res.data?.total);
        //  隐藏图表 title
        if (res.data.records) {
          res.data.records.forEach((data) => {
            const chartData = JSON.parse(data.genChart ?? '{}');
            chartData.title = undefined;
            data.genChart = JSON.stringify(chartData);
          });
        }
      } else {
        throw new Error(res.message);
      }
    } catch (e) {
      message.error(e.message, 5);
    }
    setLoading(false);
  };

  useEffect(() => {
    loadData();
  }, [searchParams]);

  return (
    <div className="myChart">
      <div>
        <Search
          placeholder="请输入图表名称"
          loading={loading}
          enterButton
          onSearch={(value) => {
            //  设置搜索条件
            setSearchParams({
              ...initParams,
              name: value,
            });
          }}
        />
      </div>
      <div className={"margin-16"} />
      <List
        grid={{
          gutter: 16,
          xs: 1,
          sm: 1,
          md: 1,
          lg: 2,
          xl: 2,
          xxl: 3,
        }}
        loading={loading}
        itemLayout="vertical"
        pagination={{
          current: searchParams.current,
          pageSize: searchParams.pageSize,
          total: total,
          onChange: (page, pageSize) => {
            setSearchParams({
              ...searchParams,
              current: page,
              pageSize: pageSize,
            });
          },
        }}
        dataSource={chartList}
        renderItem={(item) => (
          <List.Item key={item.id}>
            <Card>
              <List.Item.Meta
                avatar={<Avatar src={currentUser?.userAvatar} />}
                title={item.name}
                description={item.chartType ? '图表类型：' + item.chartType : undefined}
              />
              {`分析目标是：${item.goal}`}
            </Card>

            <Card>
              <ReactECharts option={JSON.parse(item.genChart) ?? '{}'} />
            </Card>
          </List.Item>
        )}
      />
    </div>
  );
};
export default MyChart;
