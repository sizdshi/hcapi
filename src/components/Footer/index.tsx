import { GithubOutlined } from '@ant-design/icons';
import { DefaultFooter } from '@ant-design/pro-components';
import '@umijs/max';
import React from 'react';

const Footer: React.FC = () => {
    const defaultMessage = 'Nero';
    const currentYear = new Date().getFullYear();
    return (
        <DefaultFooter
            style={{
                background: 'none',
            }}
            copyright={`${currentYear} ${defaultMessage}`}
            links={[
                {
                    key: 'Ant Design Pro',
                    title: 'Panda API',
                    href: 'http://localhost:8101',
                    blankTarget: true,
                },
                {
                    key: 'github',
                    title: <GithubOutlined />,
                    href: 'https://github.com/ichensw/',
                    blankTarget: true,
                },
                {
                    key: 'Ant Design',
                    title: 'Ant Design',
                    href: 'https://ant.design',
                    blankTarget: true,
                },
            ]}
        />
    );
};
export default Footer;
