import React, { PureComponent } from 'react';
import { Form, Modal, Input, Select } from 'antd';
import request from '@/utils/request';
import { stringify } from 'qs';

const gridStyle = {
  width: '33%',
  textAlign: 'center',
};
const { Option } = Select;


const FormItem = Form.Item;

@Form.create()
class AddContainer extends PureComponent {

  submit = ()=>{


    this.props.form.validateFields((err, values) => {
      if (!err) {
        console.log('Received values of form: ', values);
        request(`/deploy?${stringify(values)}` )
        this.props.onCancle()
      }
    });
  };

  render() {

    const { getFieldDecorator } = this.props.form;

    const formItemLayout = {
      labelCol: {
        xs: { span: 24 },
        sm: { span: 8 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 12 },
      },
    };

    return (
      <Modal
        title="添加新Container"
        visible={this.props.show}
        onOk={this.submit}
        onCancel={this.props.onCancle}
        width={600}
      >
        <Form>
          <FormItem {...formItemLayout} label="Docker Image Name">
            {getFieldDecorator('image', {
              rules: [{ required: true, message: 'Please input Docker Image Name' }],
            })(<Input />)}
          </FormItem>

          <FormItem {...formItemLayout} label="Docker Image Tag">
            {getFieldDecorator('tag', {
              rules: [{ required: true, message: 'Please input Docker Image Tag' }],
            })(<Input />)}
          </FormItem>

          <FormItem {...formItemLayout} label="CPU Vcores">
            {getFieldDecorator('vcores', {
              rules: [{ required: true, message: 'Please input vcores numbers!' }],
            })(<Input />)}
          </FormItem>

          <FormItem {...formItemLayout} label="memory">
            {getFieldDecorator('memory', {
              rules: [{ required: true, message: 'Please select container memory!' }],
            })(
              <Select>
                <Option value="1">1G</Option>
                <Option value="2">2G</Option>
                <Option value="3">3G</Option>
                <Option value="4">4G</Option>
              </Select>
            )}
          </FormItem>

          <FormItem {...formItemLayout} label="Number">
            {getFieldDecorator('number', {
              rules: [{ required: true, message: 'Please input number!' }],
            })(<Input />)}
          </FormItem>

        </Form>
      </Modal>
    );
  }
}

export default AddContainer;
