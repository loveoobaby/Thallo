import styles from './index.css';
import { Row, Col } from 'antd';
import { Table, Divider, Tag, Card, Menu, Button } from 'antd';

export default function() {

  const columns = [{
    title: 'Name',
    dataIndex: 'name',
    key: 'name',
    render: text => <a href="javascript:;">{text}</a>,
  }, {
    title: 'Age',
    dataIndex: 'age',
    key: 'age',
  }, {
    title: 'Address',
    dataIndex: 'address',
    key: 'address',
  }, {
    title: 'Tags',
    key: 'tags',
    dataIndex: 'tags',
    render: tags => (
      <span>
      {tags.map(tag => <Tag color="blue" key={tag}>{tag}</Tag>)}
    </span>
    ),
  }, {
    title: 'Action',
    key: 'action',
    render: (text, record) => (
      <span>
      <a href="javascript:;">Invite {record.name}</a>
      <Divider type="vertical"/>
      <a href="javascript:;">Delete</a>
    </span>
    ),
  }];

  const data = [{
    key: '1',
    name: 'John Brown',
    age: 32,
    address: 'New York No. 1 Lake Park',
    tags: ['nice', 'developer'],
  }, {
    key: '2',
    name: 'Jim Green',
    age: 42,
    address: 'London No. 1 Lake Park',
    tags: ['loser'],
  }, {
    key: '3',
    name: 'Joe Black',
    age: 32,
    address: 'Sidney No. 1 Lake Park',
    tags: ['cool', 'teacher'],
  }];


  const operations = (
    <div>
      <Button type={'primary'} style={{marginRight: 30}}>
        Add New Container
      </Button>
      <Button type={'danger'}>
        Stop Application
      </Button>
    </div>
  );


  return (
    <div>
      <Row>
        <Col span={4}>
          <h1 className={styles.title}>Thallo</h1>
        </Col>
        <Col span={16}>
          <h1 className={styles.title}>Ap0plicationId = </h1>
        </Col>
        <Col span={4}>
          <h1 className={styles.title}></h1>
        </Col>
      </Row>

      <Card
        title={<h1>All Containers</h1>}
        headStyle={{ textAlign: 'left' }}
        extra={operations}
      >
        <Table columns={columns} dataSource={data}/>
      </Card>

      <Card
        title={<h1>All Containers</h1>}
        headStyle={{ textAlign: 'left' }}
        extra={operations}
      >
        <Table columns={columns} dataSource={data}/>
      </Card>

    </div>

  );
}
