import {genChartByAiAsyncMqUsingPOST, genChartByAiAsyncUsingPOST} from '@/services/smart-bi/chartController';
import { UploadOutlined } from '@ant-design/icons';
import { Button, Card, Form, Input, Select, Space, Upload, message } from 'antd';
import { useForm } from 'antd/es/form/Form';
import TextArea from 'antd/es/input/TextArea';
import React, { useState } from 'react';

/**
 * 添加图表页面（异步）
 * @constructor
 */
const AddChartAsync: React.FC = () => {
  const [form] = useForm();
  const [loading, setLoading] = useState<boolean>(false);
  const onFinish = async (values: any) => {
    //  避免重复提交
    if (loading) {
      return;
    }
    setLoading(true);
    const params = {
      ...values,
      file: undefined,
    };
    try {
      //todo 文件上传到大小限制
      const res = await genChartByAiAsyncMqUsingPOST(params, {}, values.file.file.originFileObj);
      if (!res?.data) {
        throw new Error(res?.message);
      } else {
        message.success('分析任务提交成功，请稍后于"我的图表"页面查看', 5);
        form.resetFields();
      }
    } catch (e) {
      message.error(e.message, 5);
    }
    setLoading(false);
  };
  return (
    <div className="add-chart-async">
      <Card title={'智能分析(异步)'}>
        <Form
          form={form}
          name="分析"
          wrapperCol={12}
          labelAlign={'left'}
          labelCol={6}
          onFinish={onFinish}
          initialValues={{}}
        >
          <Form.Item
            name="goal"
            label="分析目标"
            rules={[{ required: true, message: '请输入分析目标' }]}
          >
            <TextArea placeholder={'请输入你的分析诉求，例：分析网站用户变化趋势'} />
          </Form.Item>
          <Form.Item name="name" label="图表名称">
            <Input placeholder={'请输入你的图表名称'} />
          </Form.Item>

          <Form.Item name="chartType" label="图表类型" hasFeedback>
            <Select
              placeholder="请输入你的分析目标"
              options={[
                { value: '折线图', label: '折线图' },
                { value: '柱状图', label: '柱状图' },
                { value: '雷达图', label: '雷达图' },
                { value: '堆叠图', label: '堆叠图' },
                { value: '饼图', label: '饼图' },
              ]}
            />
          </Form.Item>

          <Form.Item name={'file'} valuePropName={'file'} label={'选择文件 excel 或 csv'}>
            <Upload name="file" listType="picture" maxCount={1}>
              <Button icon={<UploadOutlined />}>上传文件(excel/csv)</Button>
            </Upload>
          </Form.Item>

          <Form.Item wrapperCol={{ span: 12, offset: 9 }}>
            <Space>
              <Button type="primary" htmlType="submit" loading={loading} disabled={loading}>
                提交
              </Button>
              <Button htmlType="reset" disabled={loading}>
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};
export default AddChartAsync;
